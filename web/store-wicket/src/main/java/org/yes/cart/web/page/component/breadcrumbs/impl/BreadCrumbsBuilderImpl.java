/*
 * Copyright 2009 Denys Pavlov, Igor Azarnyi
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.yes.cart.web.page.component.breadcrumbs.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.cache.annotation.Cacheable;
import org.yes.cart.constants.Constants;
import org.yes.cart.domain.entity.Category;
import org.yes.cart.domain.i18n.I18NModel;
import org.yes.cart.domain.misc.Pair;
import org.yes.cart.search.PriceNavigation;
import org.yes.cart.search.query.ProductSearchQueryBuilder;
import org.yes.cart.search.util.SearchUtil;
import org.yes.cart.service.domain.AttributeService;
import org.yes.cart.service.domain.CategoryService;
import org.yes.cart.service.domain.ContentService;
import org.yes.cart.service.domain.ShopService;
import org.yes.cart.util.DomainApiUtils;
import org.yes.cart.util.TimeContext;
import org.yes.cart.web.page.component.breadcrumbs.BreadCrumbsBuilder;
import org.yes.cart.web.page.component.breadcrumbs.Crumb;
import org.yes.cart.web.support.constants.CentralViewLabel;
import org.yes.cart.web.support.constants.WebParametersKeys;
import org.yes.cart.web.support.service.CurrencySymbolService;
import org.yes.cart.web.util.WicketUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bread crumbs builder produce category and
 * attributive filtered navigation breadcrumbs based on
 * web query string and context.
 * <p/>
 * <p/>
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 2011-May-17
 * Time: 9:50:51 AM
 */
public class BreadCrumbsBuilderImpl implements BreadCrumbsBuilder {

    private final ShopService shopService;
    private final CategoryService categoryService;
    private final ContentService contentService;
    private final CurrencySymbolService currencySymbolService;
    private final PriceNavigation priceNavigation;
    private final AttributeService attributeService;
    private final WicketUtil wicketUtil;

    /**
     * @param shopService           shop service
     * @param categoryService       category service
     * @param contentService        content service
     * @param currencySymbolService currency symbols for price crumbs
     * @param priceNavigation       price navigation
     * @param attributeService      attribute service
     * @param wicketUtil            wicket utility bean
     */
    public BreadCrumbsBuilderImpl(final ShopService shopService,
                                  final CategoryService categoryService,
                                  final ContentService contentService,
                                  final CurrencySymbolService currencySymbolService,
                                  final PriceNavigation priceNavigation,
                                  final AttributeService attributeService,
                                  final WicketUtil wicketUtil) {
        this.shopService = shopService;

        this.categoryService = categoryService;
        this.contentService = contentService;
        this.currencySymbolService = currencySymbolService;
        this.priceNavigation = priceNavigation;
        this.attributeService = attributeService;
        this.wicketUtil = wicketUtil;
    }


    /** {@inheritDoc} */
    @Override
    @Cacheable(value = "breadCrumbBuilder-breadCrumbs")
    public List<Crumb> getBreadCrumbs(final String locale,
                                      final long shopId,
                                      final long customerShopId,
                                      final long categoryId,
                                      final PageParameters pageParameters,
                                      final Set<Long> shopCategoryIds,
                                      final String pricePrefix,
                                      final String queryPrefix,
                                      final String tagPrefix) {

        final List<Crumb> crumbs = new ArrayList<>();
        final boolean isContent = pageParameters.getNamedKeys().contains(WebParametersKeys.CONTENT_ID);
        crumbs.addAll(getCategoriesCrumbs(shopId, customerShopId, categoryId, shopCategoryIds, isContent));
        crumbs.addAll(getFilteredNavigationCrumbs(locale, pageParameters, pricePrefix, queryPrefix, tagPrefix));
        return crumbs;
    }

    private List<Crumb> getFilteredNavigationCrumbs(final String locale,
                                                    final PageParameters pageParameters,
                                                    final String pricePrefix,
                                                    final String queryPrefix,
                                                    final String tagPrefix) {
        final List<Crumb> navigationCrumbs = new ArrayList<>();
        fillAttributes(locale, navigationCrumbs, pageParameters, pricePrefix, queryPrefix, tagPrefix);
        return navigationCrumbs;
    }

    private List<Crumb> getCategoriesCrumbs(final long shopId,
                                            final long customerShopId,
                                            final long categoryId,
                                            final Set<Long> shopCategoryIds,
                                            final boolean isContent) {
        final List<Crumb> crumbs = new ArrayList<>();
        if (categoryId > 0) {
            if (isContent) {
                fillContent(crumbs, shopId, categoryId, shopCategoryIds, now());
            } else {
                fillCategories(crumbs, customerShopId, categoryId, shopCategoryIds, now());
            }
        }
        return crumbs;
    }


    /**
     * Recursive function to reverse build the breadcrumbs by categories, starting from currently selected one.
     *
     * @param categoriesCrumbs the crumbs list
     * @param shopId           the current shop id
     * @param categoryId       the current category id
     * @param now              for availability check
     */
    private boolean fillContent(final List<Crumb> categoriesCrumbs,
                                final long shopId,
                                final long categoryId,
                                final Set<Long> shopCategoryIds,
                                final LocalDateTime now) {
        if (categoryId > 0L && shopCategoryIds.contains(categoryId)) {
            final Category category = categoryService.getById(categoryId);
            if (!category.isRoot() && !CentralViewLabel.INCLUDE.equals(category.getUitemplate())) {

                if (!DomainApiUtils.isObjectAvailableNow(!category.isDisabled(), category.getAvailablefrom(), category.getAvailableto(), now)) {
                    return false; // Not available
                }

                boolean parentAvailable = true;

                final long parentId = category.getParentId();
                if (parentId > 0L) {
                    parentAvailable = fillContent(categoriesCrumbs, shopId, parentId, shopCategoryIds, now);
                }

                if (parentAvailable) {
                    categoriesCrumbs.add(
                            new Crumb("category", category.getName(),
                                    category.getDisplayName(), getCategoryLinkParameters(categoryId, true),
                                    getRemoveCategoryLinkParameters(category, shopCategoryIds, true)
                            )
                    );
                }

                return parentAvailable;

            }
        }
        return true; // reached the top
    }

    /**
     * Recursive function to reverse build the breadcrumbs by categories, starting from currently selected one.
     *
     * @param categoriesCrumbs the crumbs list
     * @param customerShopId   the current shop id
     * @param categoryId       the current category id
     * @param now              for availability check
     */
    private boolean fillCategories(final List<Crumb> categoriesCrumbs,
                                   final long customerShopId,
                                   final long categoryId,
                                   final Set<Long> shopCategoryIds,
                                   final LocalDateTime now) {
        if (categoryId > 0L && shopCategoryIds.contains(categoryId)) {
            final Category category = categoryService.getById(categoryId);
            if (!category.isRoot()) {

                if (!DomainApiUtils.isObjectAvailableNow(!category.isDisabled(), category.getAvailablefrom(), category.getAvailableto(), now)) {
                    return false; // Not available
                }

                boolean parentAvailable = true;

                final Long parentId = shopService.getShopCategoryParentId(customerShopId, categoryId);
                if (parentId != null && parentId > 0L) {
                    parentAvailable = fillCategories(categoriesCrumbs, customerShopId, parentId, shopCategoryIds, now);
                }

                if (parentAvailable) {
                    categoriesCrumbs.add(
                            new Crumb("category", category.getName(),
                                    category.getDisplayName(), getCategoryLinkParameters(categoryId, false),
                                    getRemoveCategoryLinkParameters(category, shopCategoryIds, false)
                            )
                    );
                }

                return parentAvailable;

            }
        }
        return true; // reached the top
    }

    private LocalDateTime now() {
        return TimeContext.getLocalDateTime();
    }

    /**
     * Get {@link PageParameters}, that point to given category.
     *
     *
     * @param categoryId given category id
     * @param isContent  true if given category is content, false if given category is category
     *
     * @return page parameters for link
     */
    private PageParameters getCategoryLinkParameters(final long categoryId, final boolean isContent) {
        return new PageParameters().add(isContent ? WebParametersKeys.CONTENT_ID : WebParametersKeys.CATEGORY_ID, categoryId);
    }

    /**
     * Get {@link PageParameters}, that point to parent, if any, of given category.
     *
     *
     * @param category given category
     * @param isContent  true if given category is content, false if given category is category
     *
     * @return page parameter for point to parent.
     */
    private PageParameters getRemoveCategoryLinkParameters(final Category category, final Set<Long> shopCategoryIds, final boolean isContent) {
        if (shopCategoryIds.contains(category.getParentId())) {
            final Category parent = categoryService.getById(category.getParentId());
            if (parent != null && !parent.isRoot() && !CentralViewLabel.INCLUDE.equals(parent.getUitemplate())) {
                return getCategoryLinkParameters(parent.getCategoryId(), isContent);
            }
        }
        return new PageParameters();
    }

    private void fillAttributes(final String locale,
                                final List<Crumb> navigationCrumbs,
                                final PageParameters pageParameters,
                                final String pricePrefix,
                                final String queryPrefix,
                                final String tagPrefix) {

        final Set<String> allowedAttributeNames = attributeService.getAllNavigatableAttributeCodes();
        /*
           Call below creates very unproductive query for all attribute codes, so we
           use a separate method for that:
            this.attributeCodeName = attributeService.getAttributeNamesByCodes(allowedAttributeNames);
         */
        final Map<String, I18NModel> attributeCodeName = attributeService.getAllAttributeNames();


        // This is attributive only filtered navigation from request
        final PageParameters attributesOnly = wicketUtil.getRetainedRequestParameters(
                pageParameters,
                allowedAttributeNames);

        // Base hold category path from beginning and accumulate all attributive navigation
        final PageParameters base = wicketUtil.getFilteredRequestParameters(
                pageParameters,
                allowedAttributeNames);

        //If we are on display product page, we have to remove for filtering  as well as sku
        base.remove(WebParametersKeys.PRODUCT_ID);
        base.remove(WebParametersKeys.SKU_ID);

        for (PageParameters.NamedPair namedPair : attributesOnly.getAllNamed()) {

            final String displayValueName = determineDisplayValueName(namedPair.getKey(), namedPair.getValue(), locale);
            navigationCrumbs.add(createFilteredNavigationCrumb(
                    base, namedPair.getKey(), namedPair.getValue(), displayValueName, locale, pageParameters,
                    pricePrefix, queryPrefix, tagPrefix, attributeCodeName));
        }
    }

    private String determineDisplayValueName(final String code, final String rawValue, final String locale) {
        if (ProductSearchQueryBuilder.PRODUCT_PRICE.equals(code)) {
            Pair<String, Pair<BigDecimal, BigDecimal>> pair = priceNavigation.decomposePriceRequestParams(rawValue);
            Pair<String, Boolean> symbol = currencySymbolService.getCurrencySymbol(pair.getFirst());
            final StringBuilder displayPrice = new StringBuilder();
            if (symbol.getSecond()) {
                displayPrice.append(pair.getSecond().getFirst().toPlainString()).append(' ').append(symbol.getFirst());
                displayPrice.append(" ... ");
                displayPrice.append(pair.getSecond().getSecond().toPlainString()).append(' ').append(symbol.getFirst());
            } else {
                displayPrice.append(symbol.getFirst()).append(' ').append(pair.getSecond().getFirst().toPlainString());
                displayPrice.append(" ... ");
                displayPrice.append(symbol.getFirst()).append(' ').append(pair.getSecond().getSecond().toPlainString());
            }
            return displayPrice.toString();
        } else if (rawValue != null && rawValue.contains(Constants.RANGE_NAVIGATION_DELIMITER)) {
            final String[] range = StringUtils.split(rawValue, Constants.RANGE_NAVIGATION_DELIMITER);
            if (range.length == 2) {
                final I18NModel fromDisplayValue = attributeService.getNavigatableAttributeDisplayValue(code, SearchUtil.longToVal(range[0], Constants.NUMERIC_NAVIGATION_PRECISION));
                final I18NModel toDisplayValue = attributeService.getNavigatableAttributeDisplayValue(code, SearchUtil.longToVal(range[1], Constants.NUMERIC_NAVIGATION_PRECISION));
                return fromDisplayValue.getValue(locale) + " - " + toDisplayValue.getValue(locale);
            }
            return null;
        } else {
            final I18NModel displayValue = attributeService.getNavigatableAttributeDisplayValue(code, rawValue);
            return displayValue.getValue(locale);
        }
    }

    /**
     * Create filtered navigation crumb with two links:
     * <p/>
     * First - current position, that include the whole path before current.
     * example category/17/subcategory/156/price/100-200/currentkey/currentvalue
     * <p/>
     * Second - the whole current path without current
     * example category/17/subcategory/156/price/100-200/currentkey/currentvalue/nextkey/nextvalue
     * ^^^^^^^^^^^^^^^^^^^^^^^ this will be removed,
     * so uri will be
     * example category/17/subcategory/156/price/100-200/nextkey/nextvalue
     */
    private Crumb createFilteredNavigationCrumb(final PageParameters base,
                                                final String key,
                                                final String value,
                                                final String displayValue,
                                                final String locale,
                                                final PageParameters pageParameters,
                                                final String pricePrefix,
                                                final String queryPrefix,
                                                final String tagPrefix,
                                                final Map<String, I18NModel> attributeCodeName) {

        final PageParameters withoutCurrent = wicketUtil.getFilteredRequestParameters(
                pageParameters,
                key,
                value
        );

        String linkName = getLinkNamePrefix(key, locale, pricePrefix, queryPrefix, tagPrefix, attributeCodeName);
        if (StringUtils.isNotBlank(linkName)) {
            linkName += "::" + getLinkName(key, value, displayValue);
        } else {
            linkName = getLinkName(key, value, displayValue);
        }

        base.add(key, value);
        return new Crumb(key, linkName, null, new PageParameters(base), withoutCurrent);
    }

    private String getLinkNamePrefix(final String key,
                                     final String locale,
                                     final String pricePrefix,
                                     final String queryPrefix,
                                     final String tagPrefix,
                                     final Map<String, I18NModel> attributeCodeName) {
        final String name;
        if (ProductSearchQueryBuilder.PRODUCT_PRICE.equals(key)) {
            name = pricePrefix;
        } else if (ProductSearchQueryBuilder.PRODUCT_TAG_FIELD.equals(key)) {
            name = tagPrefix;
        } else if (WebParametersKeys.QUERY.equals(key)) {
            name = queryPrefix;
        } else {
            final I18NModel nameModel = attributeCodeName.get(key);
            if (nameModel == null) {
                name = "";
            } else {
                name = nameModel.getValue(locale);
            }
        }
        return name;
    }

    private String getLinkName(final String key, final String value, final String displayValue) {
        if (displayValue == null) {
            return value;
        }
        return displayValue;
    }



}
