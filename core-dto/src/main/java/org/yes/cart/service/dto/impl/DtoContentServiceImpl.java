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

package org.yes.cart.service.dto.impl;

import com.inspiresoftware.lib.dto.geda.adapter.repository.AdaptersRepository;
import com.inspiresoftware.lib.dto.geda.assembler.Assembler;
import com.inspiresoftware.lib.dto.geda.assembler.DTOAssembler;
import org.apache.commons.lang.StringUtils;
import org.yes.cart.constants.AttributeGroupNames;
import org.yes.cart.constants.Constants;
import org.yes.cart.dao.GenericDAO;
import org.yes.cart.domain.dto.AttrValueCategoryDTO;
import org.yes.cart.domain.dto.AttrValueDTO;
import org.yes.cart.domain.dto.AttributeDTO;
import org.yes.cart.domain.dto.CategoryDTO;
import org.yes.cart.domain.dto.factory.DtoFactory;
import org.yes.cart.domain.dto.impl.AttrValueCategoryDTOImpl;
import org.yes.cart.domain.dto.impl.CategoryDTOImpl;
import org.yes.cart.domain.entity.*;
import org.yes.cart.domain.entity.impl.AttrValueEntityCategory;
import org.yes.cart.domain.misc.Pair;
import org.yes.cart.exception.UnableToCreateInstanceException;
import org.yes.cart.exception.UnmappedInterfaceException;
import org.yes.cart.service.domain.*;
import org.yes.cart.service.dto.DtoAttributeService;
import org.yes.cart.service.dto.DtoContentService;
import org.yes.cart.utils.impl.AttrValueDTOComparatorImpl;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Denis Pavlov
 * Date: 15-June-2013
 */
public class DtoContentServiceImpl
        extends AbstractDtoServiceImpl<CategoryDTO, CategoryDTOImpl, Category>
        implements DtoContentService {

    private static final CategoryRankNameComparator CATEGORY_RANK_NAME_COMPARATOR = new CategoryRankNameComparator();

    private final GenericService<ProductType> productTypeService;

    private final GenericService<Attribute> attributeService;
    private final DtoAttributeService dtoAttributeService;
    private final GenericDAO<AttrValueEntityCategory, Long> attrValueEntityCategoryDao;

    private final Assembler attrValueAssembler;

    private final ImageService imageService;
    private final FileService fileService;
    private final SystemService systemService;

    /**
     * Construct base remote service.
     *
     * @param dtoFactory             {@link org.yes.cart.domain.dto.factory.DtoFactory}
     * @param categoryGenericService category     {@link org.yes.cart.service.domain.GenericService}
     * @param imageService           {@link org.yes.cart.service.domain.ImageService} to manipulate  related images.
     * @param fileService {@link FileService} to manipulate related files
     * @param systemService          system service
     */
    public DtoContentServiceImpl(final DtoFactory dtoFactory,
                                 final GenericService<Category> categoryGenericService,
                                 final GenericService<ProductType> productTypeService,
                                 final DtoAttributeService dtoAttributeService,
                                 final GenericDAO<AttrValueEntityCategory, Long> attrValueEntityCategoryDao,
                                 final ImageService imageService,
                                 final FileService fileService,
                                 final AdaptersRepository adaptersRepository,
                                 final SystemService systemService) {
        super(dtoFactory, categoryGenericService, adaptersRepository);


        this.productTypeService = productTypeService;
        this.attrValueEntityCategoryDao = attrValueEntityCategoryDao;
        this.dtoAttributeService = dtoAttributeService;
        this.systemService = systemService;

        this.attributeService = dtoAttributeService.getService();


        this.attrValueAssembler = DTOAssembler.newAssembler(
                dtoFactory.getImplClass(AttrValueCategoryDTO.class),
                attributeService.getGenericDao().getEntityFactory().getImplClass(AttrValueCategory.class));

        this.imageService = imageService;
        this.fileService = fileService;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<CategoryDTO> getAll() throws UnmappedInterfaceException, UnableToCreateInstanceException {
        throw new UnsupportedOperationException("Use getAllFromRoot()");
    }

    /**
     * {@inheritDoc}
     */
    public void createContentRoot(final long shopId) {
        ((ContentService) service).createRootContent(shopId);
    }

    /**
     * {@inheritDoc}
     */
    public List<CategoryDTO> getAllWithAvailabilityFilter(final long shopId, final boolean withAvailabilityFiltering)
            throws UnmappedInterfaceException, UnableToCreateInstanceException {
        ContentService contentService = (ContentService) service;
        Category root = contentService.getRootContent(shopId);
        if (root != null) {
            CategoryDTO rootDTO = getById(root.getCategoryId());
            loadBranch(rootDTO, withAvailabilityFiltering, Integer.MAX_VALUE, Collections.emptyList());
            return Collections.singletonList(rootDTO);
        }
        return null;
    }


    private List<CategoryDTO> loadBranch(final CategoryDTO rootDTO,
                                         final boolean withAvailabilityFiltering,
                                         final int expandLevel,
                                         final List<Long> expandNodes)
            throws UnmappedInterfaceException, UnableToCreateInstanceException {
        if (rootDTO != null) {
            final ContentService contentService = (ContentService) service;
            final List<Category> childContent = new ArrayList<Category>(contentService.findChildContentWithAvailability(
                    rootDTO.getCategoryId(),
                    withAvailabilityFiltering));
            Collections.sort(childContent, CATEGORY_RANK_NAME_COMPARATOR);
            final List<CategoryDTO> childCategoriesDTO = new ArrayList<CategoryDTO>(childContent.size());
            fillDTOs(childContent, childCategoriesDTO);
            rootDTO.setChildren(childCategoriesDTO);
            if (expandLevel > 1 || !expandNodes.isEmpty()) {
                for (CategoryDTO dto : childCategoriesDTO) {
                    if (expandLevel > 1 || expandNodes.contains(dto.getCategoryId())) {
                        dto.setChildren(loadBranch(dto, withAvailabilityFiltering, expandLevel - 1, expandNodes));
                    }
                }
            }
            return childCategoriesDTO;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<CategoryDTO> getBranchById(final long shopId, final long categoryId, final List<Long> expand)
            throws UnmappedInterfaceException, UnableToCreateInstanceException {
        return getBranchByIdWithAvailabilityFilter(shopId, categoryId, false, expand);
    }

    /**
     * {@inheritDoc}
     */
    public List<CategoryDTO> getBranchByIdWithAvailabilityFilter(final long shopId, final long contentId, final boolean withAvailabilityFiltering, final List<Long> expand)
            throws UnmappedInterfaceException, UnableToCreateInstanceException {

        ContentService contentService = (ContentService) service;
        final Category branchRoot = contentId > 0L ? contentService.getById(contentId) : contentService.getRootContent(shopId);
        if (branchRoot != null) {
            CategoryDTO rootDTO = getById(branchRoot.getCategoryId());
            if (rootDTO != null) {
                loadBranch(rootDTO, withAvailabilityFiltering, 1, expand != null ? expand : Collections.emptyList());
            }
            return Collections.singletonList(rootDTO);
        }
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    protected void assemblyPostProcess(final CategoryDTO dto, final Category entity) {
        dto.setParentName(getParentName(entity));
        super.assemblyPostProcess(dto, entity);
    }

    protected String getParentName(final Category entity) {
        if (entity.getParentId() > 0L && entity.getParentId() != entity.getCategoryId()) {
            final Category parent = ((ContentService)getService()).getById(entity.getParentId());
            if (parent != null) {
                final String oneUp = getParentName(parent);
                if (oneUp != null) {
                    return oneUp + " > " + parent.getName();
                }
                return parent.getName();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected void createPostProcess(final CategoryDTO dto, final Category entity) {
        bindDictionaryData(dto, entity);
        ensureBlankUriIsNull(entity);
        super.createPostProcess(dto, entity);
    }

    /**
     * {@inheritDoc}
     */
    protected void updatePostProcess(final CategoryDTO dto, final Category entity) {
        bindDictionaryData(dto, entity);
        ensureBlankUriIsNull(entity);
        super.updatePostProcess(dto, entity);
    }


    private void ensureBlankUriIsNull(final Seoable entity) {
        if (entity.getSeo() != null && entity.getSeo().getUri() != null && StringUtils.isBlank(entity.getSeo().getUri())) {
            entity.getSeo().setUri(null);
        }
    }

    /**
     * Bind data from dictionaries to category.
     *
     * @param instance category dto to collect data from
     * @param category category to set dictionary data to.
     */
    private void bindDictionaryData(final CategoryDTO instance, final Category category) {
        if (instance.getProductTypeId() != null && instance.getProductTypeId() > 0) {
            category.setProductType(productTypeService.findById(instance.getProductTypeId()));
        } else {
            category.setProductType(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<CategoryDTO> getAllByShopId(final long shopId) throws UnmappedInterfaceException, UnableToCreateInstanceException {
        return getAllWithAvailabilityFilter(shopId, false);
    }


    private final static char[] PARENT_OR_URI = new char[] { '^', '@' };
    static {
        Arrays.sort(PARENT_OR_URI);
    }

    /**
     * {@inheritDoc}
     */
    public List<CategoryDTO> findBy(final long shopId, final String filter, final int page, final int pageSize) throws UnmappedInterfaceException, UnableToCreateInstanceException {

        ContentService contentService = (ContentService) service;

        final List<CategoryDTO> contentDTO = new ArrayList<CategoryDTO>(pageSize);

        if (StringUtils.isNotBlank(filter)) {
            final Pair<String, String> parentOrUri = ComplexSearchUtils.checkSpecialSearch(filter, PARENT_OR_URI);

            if (parentOrUri == null) {

                fillDTOs(contentService.findBy(shopId, filter, filter, filter, page, pageSize), contentDTO);

            } else {

                if ("@".equals(parentOrUri.getFirst())) {

                    fillDTOs(contentService.findBy(shopId, null, null, parentOrUri.getSecond(), page, pageSize), contentDTO);

                } else if ("^".equals(parentOrUri.getFirst())) {

                    final List<Category> parents = contentService.findBy(shopId, parentOrUri.getSecond(), parentOrUri.getSecond(), parentOrUri.getSecond(), page, pageSize);

                    if (!parents.isEmpty()) {

                        final Set<Long> dedup = new HashSet<Long>();
                        final List<Category> parentsWithChildren = new ArrayList<Category>();
                        for (final Category parent : parents) {

                            if (!dedup.contains(parent.getCategoryId())) {
                                parentsWithChildren.add(parent);
                                dedup.add(parent.getCategoryId());
                            }
                            for (final Category child : contentService.findChildContentWithAvailability(parent.getCategoryId(), false)) {
                                if (!dedup.contains(child.getCategoryId())) {
                                    parentsWithChildren.add(child);
                                    dedup.add(child.getCategoryId());
                                }
                            }

                        }

                        fillDTOs(parentsWithChildren, contentDTO);

                    }

                }

            }

        } else {

            fillDTOs(contentService.findBy(shopId, null, null, null, page, pageSize), contentDTO);

        }

        return contentDTO;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isUriAvailableForContent(final String seoUri, final Long contentId) {

        final Long conId = ((ContentService) service).findContentIdBySeoUri(seoUri);
        return conId == null || conId.equals(contentId);

    }


    /**
     * {@inheritDoc}
     */
    public boolean isGuidAvailableForContent(final String guid, final Long contentId) {

        final Long conId = ((ContentService) service).findContentIdByGUID(guid);
        return conId == null || conId.equals(contentId);

    }

    /**
     * Get the dto interface.
     *
     * @return dto interface.
     */
    public Class<CategoryDTO> getDtoIFace() {
        return CategoryDTO.class;
    }

    /**
     * Get the dto implementation class.
     *
     * @return dto implementation class.
     */
    public Class<CategoryDTOImpl> getDtoImpl() {
        return CategoryDTOImpl.class;
    }

    /**
     * Get the entity interface.
     *
     * @return entity interface.
     */
    public Class<Category> getEntityIFace() {
        return Category.class;
    }

    /**
     * {@inheritDoc}
     */
    public List<? extends AttrValueDTO> getEntityContentAttributes(final long entityPk) throws UnmappedInterfaceException, UnableToCreateInstanceException {
        final List<AttrValueCategoryDTO> result = new ArrayList<AttrValueCategoryDTO>();
        final CategoryDTO categoryDTO = getById(entityPk);
        if (categoryDTO != null) {
            final List<AttributeDTO> availableAttributeDTOs = dtoAttributeService.findAvailableAttributes(
                    AttributeGroupNames.CATEGORY, Collections.EMPTY_LIST);
            final Map<String, AttributeDTO> contentAttrsDTOs = new HashMap<String, AttributeDTO>();
            for (final AttributeDTO attributeDTO : availableAttributeDTOs) {
                final Matcher matcher = CONTENT_BODY_PART.matcher(attributeDTO.getCode());
                if (matcher.find()) {
                    final String locale = matcher.group(1);
                    final String key = "CONTENT_BODY_" + locale;
                    if (!contentAttrsDTOs.containsKey(key)) {
                        final AttributeDTO global = dtoFactory.getByIface(AttributeDTO.class);
                        global.setCode(key);
                        global.setName(attributeDTO.getName());
                        global.setDisplayNames(attributeDTO.getDisplayNames());
                        global.setDescription(attributeDTO.getDescription());
                        global.setEtypeId(attributeDTO.getEtypeId());
                        global.setEtypeName(attributeDTO.getEtypeName());
                        contentAttrsDTOs.put(key, global);
                    }
                }
            }


            for (final AttributeDTO attributeDTO : contentAttrsDTOs.values()) {
                final Map<String, String> content = new HashMap<String, String>();

                for (AttrValueCategoryDTO attributeValueDTO : categoryDTO.getAttributes()) {
                    final Matcher matcher = CONTENT_BODY_PART.matcher(attributeValueDTO.getAttributeDTO().getCode());
                    if (matcher.find()) {
                        final String locale = matcher.group(1);
                        final String part = matcher.group(2);
                        final String key = "CONTENT_BODY_" + locale;
                        if (attributeDTO.getCode().equals(key)) {
                            if (StringUtils.isNotBlank(attributeValueDTO.getVal())) {
                                content.put(part, attributeValueDTO.getVal());
                            }
                        }
                    }
                }

                final StringBuilder parts = new StringBuilder();
                for (final String partNo : new TreeSet<String>(content.keySet())) {
                    parts.append(content.get(partNo));
                }

                AttrValueCategoryDTO attrValueCategoryDTO = getAssemblerDtoFactory().getByIface(AttrValueCategoryDTO.class);
                attrValueCategoryDTO.setAttributeDTO(attributeDTO);
                attrValueCategoryDTO.setCategoryId(entityPk);
                attrValueCategoryDTO.setVal(parts.toString());

                result.add(attrValueCategoryDTO);


            }
            Collections.sort(result, new AttrValueDTOComparatorImpl());
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    public List<? extends AttrValueDTO> getEntityAttributes(final long entityPk)
            throws UnmappedInterfaceException, UnableToCreateInstanceException {
        final List<AttrValueCategoryDTO> result = new ArrayList<AttrValueCategoryDTO>();
        final CategoryDTO categoryDTO = getById(entityPk);
        if (categoryDTO != null) {
            result.addAll(categoryDTO.getAttributes());
            final List<AttributeDTO> availableAttributeDTOs = dtoAttributeService.findAvailableAttributes(
                    AttributeGroupNames.CATEGORY,
                    getCodes(result));
            for (AttributeDTO attributeDTO : availableAttributeDTOs) {
                AttrValueCategoryDTO attrValueCategoryDTO = getAssemblerDtoFactory().getByIface(AttrValueCategoryDTO.class);
                attrValueCategoryDTO.setAttributeDTO(attributeDTO);
                attrValueCategoryDTO.setCategoryId(entityPk);
                result.add(attrValueCategoryDTO);
            }
            Collections.sort(result, new AttrValueDTOComparatorImpl());
        }

        return result;
    }

    /*
     * Matcher that matches exact attribute such as CONTENT_BODY_en but not body parts
     * such as CONTENT_BODY_en_1, CONTENT_BODY_en_2 ... CONTENT_BODY_en_n.
     * This pattern allows to intercept virtual update for all content body parts.
     */
    private static final Pattern CONTENT_BODY = Pattern.compile("CONTENT_BODY_([a-z]{2})$");

    private static final Pattern CONTENT_BODY_PART = Pattern.compile("CONTENT_BODY_([a-z]{2})_(\\d+)$");

    // This is the limit on AV.val field - do not change unless changing schema
    private static final int CHUNK_SIZE = 4000;

    /**
     * Update attribute value.
     *
     * @param attrValueDTO value to update
     * @return updated value
     */
    public AttrValueDTO updateEntityAttributeValue(final AttrValueDTO attrValueDTO) {
        final Matcher matcher = CONTENT_BODY.matcher(attrValueDTO.getAttributeDTO().getCode());
        if (matcher.find()) {
            final String locale = matcher.group(1);
            final String keyStart = "CONTENT_BODY_" + locale;
            final String keyLike = keyStart + "_%";
            final String val = attrValueDTO.getVal();
            final List<Object> bodyAttrs = attrValueEntityCategoryDao
                    .findQueryObjectByNamedQuery("CONTENTBODY.ATTRIBUTES", keyLike);
            if (val.length() > bodyAttrs.size() * CHUNK_SIZE) {
                throw new IllegalArgumentException("There are " + bodyAttrs.size() + " body parts attributes for "
                        + keyLike + " which limits content to " + bodyAttrs.size() * CHUNK_SIZE
                        + " characters. Your input (" + val.length() + ") exceeds this limit. Add more body attributes.");
            }

            final Category content = service.findById(((AttrValueCategoryDTO) attrValueDTO).getCategoryId());
            final Iterator<AttrValueCategory> itOld = content.getAttributes().iterator();
            while (itOld.hasNext()) {
                final AttrValueCategory old = itOld.next();
                if (old.getAttributeCode().startsWith(keyStart)) {
                    itOld.remove();
                    attrValueEntityCategoryDao.delete(old);
                }
            }
            int pos = 0;
            int chunkCount = 0;
            String part;
            do {
                part = pos + CHUNK_SIZE > val.length() ? val.substring(pos) : val.substring(pos, pos + CHUNK_SIZE);
                Attribute atr = (Attribute) bodyAttrs.get(chunkCount);
                AttrValueCategory valueEntityCategory = getPersistenceEntityFactory().getByIface(AttrValueCategory.class);
                valueEntityCategory.setAttributeCode(atr.getCode());
                valueEntityCategory.setCategory(content);
                valueEntityCategory.setVal(part);
                attrValueEntityCategoryDao.create((AttrValueEntityCategory) valueEntityCategory);
                chunkCount++;
                pos += CHUNK_SIZE;
            } while (pos < val.length());

        } else {
            final AttrValueEntityCategory valueEntityCategory = attrValueEntityCategoryDao.findById(attrValueDTO.getAttrvalueId());
            attrValueAssembler.assembleEntity(attrValueDTO, valueEntityCategory, getAdaptersRepository(), dtoFactory);
            attrValueEntityCategoryDao.update(valueEntityCategory);
        }
        return attrValueDTO;

    }


    /**
     * Delete attribute value by given pk value.
     *
     * @param attributeValuePk given pk value.
     */
    public long deleteAttributeValue(final long attributeValuePk)
            throws UnmappedInterfaceException, UnableToCreateInstanceException{
        final AttrValueEntityCategory valueEntityCategory = attrValueEntityCategoryDao.findById(attributeValuePk);
        final AttributeDTO attributeDTO = dtoAttributeService.findByAttributeCode(valueEntityCategory.getAttributeCode());
        if (Etype.IMAGE_BUSINESS_TYPE.equals(attributeDTO.getEtypeName())) {
            imageService.deleteImage(valueEntityCategory.getVal(),
                    Constants.CATEGORY_IMAGE_REPOSITORY_URL_PATTERN, systemService.getImageRepositoryDirectory());
        } else if (Etype.FILE_BUSINESS_TYPE.equals(attributeDTO.getEtypeName())) {
            fileService.deleteFile(valueEntityCategory.getVal(),
                    Constants.CATEGORY_FILE_REPOSITORY_URL_PATTERN, systemService.getFileRepositoryDirectory());
        }

        attrValueEntityCategoryDao.delete(valueEntityCategory);
        return valueEntityCategory.getCategory().getCategoryId();
    }

    /**
     * Create attribute value
     *
     * @param attrValueDTO value to persist
     * @return created value
     */
    public AttrValueDTO createEntityAttributeValue(final AttrValueDTO attrValueDTO) {

        final Attribute atr = attributeService.findById(attrValueDTO.getAttributeDTO().getAttributeId());
        final boolean multivalue = atr.isAllowduplicate();
        final Category category = service.findById(((AttrValueCategoryDTO) attrValueDTO).getCategoryId());
        if (!multivalue) {
            for (final AttrValueCategory avp : category.getAttributes()) {
                if (avp.getAttributeCode().equals(atr.getCode())) {
                    // this is a duplicate, so need to update
                    attrValueDTO.setAttrvalueId(avp.getAttrvalueId());
                    return updateEntityAttributeValue(attrValueDTO);
                }
            }
        }

        AttrValueCategory valueEntityCategory = getPersistenceEntityFactory().getByIface(AttrValueCategory.class);
        attrValueAssembler.assembleEntity(attrValueDTO, valueEntityCategory, getAdaptersRepository(), dtoFactory);
        valueEntityCategory.setAttributeCode(atr.getCode());
        valueEntityCategory.setCategory(category);
        valueEntityCategory = attrValueEntityCategoryDao.create((AttrValueEntityCategory) valueEntityCategory);
        attrValueDTO.setAttrvalueId(valueEntityCategory.getAttrvalueId());
        return attrValueDTO;

    }

    /**
     * {@inheritDoc}
     */
    public AttrValueDTO createAndBindAttrVal(final long entityPk, final String attrName, final String attrValue)
            throws UnmappedInterfaceException, UnableToCreateInstanceException {
        throw new UnmappedInterfaceException("Not implemented");
    }


    /**
     * {@inheritDoc}
     */
    public AttrValueDTO getNewAttribute(final long entityPk) throws UnableToCreateInstanceException, UnmappedInterfaceException {
        final AttrValueCategoryDTO dto = new AttrValueCategoryDTOImpl();
        dto.setCategoryId(entityPk);
        return dto;
    }
}
