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

package org.yes.cart.web.support.entity.decorator.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.yes.cart.constants.AttributeNamesKeys;
import org.yes.cart.constants.Constants;
import org.yes.cart.domain.entity.AttrValueCategory;
import org.yes.cart.domain.entity.Category;
import org.yes.cart.domain.entity.SeoImage;
import org.yes.cart.domain.entity.impl.CategoryEntity;
import org.yes.cart.domain.i18n.impl.FailoverStringI18NModel;
import org.yes.cart.domain.misc.Pair;
import org.yes.cart.service.domain.CategoryService;
import org.yes.cart.service.domain.ImageService;
import org.yes.cart.web.support.entity.decorator.CategoryDecorator;
import org.yes.cart.web.support.i18n.I18NWebSupport;
import org.yes.cart.web.support.service.AttributableImageService;

import java.util.List;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 7/4/11
 * Time: 8:19 PM
 */
public class CategoryDecoratorImpl extends CategoryEntity implements CategoryDecorator {

    private final AttributableImageService categoryImageService;
    private final CategoryService categoryService;
    private final String httpServletContextPath;
    private final ImageService imageService;
    private final I18NWebSupport i18NWebSupport;

    /**
     * Construct entity decorator.
     *
     * @param imageService           image service to get the image seo info
     * @param categoryImageService   category image service to get the image.
     * @param categoryService        category service to get the images width and height
     * @param categoryEntity         entity to decorate.
     * @param httpServletContextPath servlet context path
     * @param i18NWebSupport         i18n support
     */
    public CategoryDecoratorImpl(final ImageService imageService,
                                 final AttributableImageService categoryImageService,
                                 final CategoryService categoryService,
                                 final Category categoryEntity,
                                 final String httpServletContextPath,
                                 final I18NWebSupport i18NWebSupport) {
        this.categoryService = categoryService;
        this.categoryImageService = categoryImageService;
        this.httpServletContextPath = httpServletContextPath;
        this.imageService = imageService;
        this.i18NWebSupport = i18NWebSupport;
        if (categoryEntity != null) {
            BeanUtils.copyProperties(categoryEntity, this);
        }
    }



    /**
     * {@inheritDoc}
     * @param lang
     */
    @Override
    public List<Pair<String, String>> getImageAttributeFileNames(final String lang) {

        return categoryImageService.getImageAttributeFileNames(this, lang);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImage(final String width, final String height, final String imageAttributeName, final String lang) {
        return categoryImageService.getImage(
                    this,
                    httpServletContextPath,
                    lang,
                    width,
                    height,
                    imageAttributeName,
                    null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultImage(final String width, final String height, final String lang) {
        final String defaultAttribute = getImageAttributeFileNames(lang).get(0).getFirst();
        return getImage(width, height, defaultAttribute, lang);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SeoImage getSeoImage(final String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        return imageService.getSeoImage(Constants.CATEGORY_IMAGE_REPOSITORY_URL_PATTERN + fileName);
    }

    /** {@inheritDoc} */
    @Override
    public String getName(final String locale) {
        return i18NWebSupport.getFailoverModel(getDisplayName(), getName()).getValue(locale);
    }

    private String getAttributeValue(final String locale, final String attribute, final String defaultValue) {
        final AttrValueCategory avc = getAttributeByCode(attribute);
        if (avc != null) {
            final String rez = new FailoverStringI18NModel(avc.getDisplayVal(), avc.getVal()).getValue(locale);
            if (StringUtils.isNotBlank(rez)) {
                return rez;
            }
        }
        return defaultValue;
    }


    /** {@inheritDoc} */
    @Override
    public String getAttributeValue(final String attribute) {
        return getAttributeValue(null, attribute, null);
    }

    /** {@inheritDoc} */
    @Override
    public String getAttributeValue(final String locale, final String attribute) {
        return getAttributeValue(locale, attribute, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription(final String locale) {
        return getAttributeValue(null, AttributeNamesKeys.Category.CATEGORY_DESCRIPTION_PREFIX + locale, getDescription());
    }
}
