package com.yinyu.service.impl;

import com.yinyu.entity.dto.*;
import com.yinyu.entity.enums.DictCodeEnum;
import com.yinyu.entity.po.SysDict;
import com.yinyu.entity.vo.DictItemVO;
import com.yinyu.entity.vo.DictTreeVO;
import com.yinyu.entity.vo.DictTypeVO;
import com.yinyu.exception.BusinessException;
import com.yinyu.mapper.SysDictMapper;
import com.yinyu.service.DictService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl implements DictService {

    private static final long ROOT_PARENT_ID = 0L;
    private static final Long ROOT_PARENT_VALUE = 0L;

    private final SysDictMapper sysDictMapper;

    public DictServiceImpl(SysDictMapper sysDictMapper) {
        this.sysDictMapper = sysDictMapper;
    }

    @Override
    public List<DictTypeVO> listTypeList(DictTypeQueryRequest request) {
        return sysDictMapper.selectTypeList(request == null ? null : request.getKeyword())
                .stream()
                .map(this::toTypeVO)
                .toList();
    }

    @Override
    public List<DictItemVO> listItemList(DictItemQueryRequest request) {
        ensureTypeExists(request.getParentId());
        return sysDictMapper.selectItemList(request.getParentId(), request.getKeyword())
                .stream()
                .map(this::toItemVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createType(DictTypeSaveRequest request) {
        validateCodeUnique(ROOT_PARENT_ID, request.getCode(), null);
        SysDict entity = new SysDict();
        entity.setParentId(ROOT_PARENT_ID);
        entity.setDictCode(request.getCode().trim());
        entity.setDictName(request.getName().trim());
        entity.setSortNum(nextSortNum(ROOT_PARENT_ID));
        entity.setStatus(request.getStatus().trim());
        entity.setRemark(trimToNull(request.getRemark()));
        sysDictMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateType(DictTypeSaveRequest request) {
        SysDict existing = requireDict(request.getId(), "dict type not found");
        if (!ROOT_PARENT_VALUE.equals(existing.getParentId())) {
            throw new BusinessException("current record is not a dict type");
        }
        validateCodeUnique(ROOT_PARENT_ID, request.getCode(), request.getId());
        existing.setDictCode(request.getCode().trim());
        existing.setDictName(request.getName().trim());
        existing.setStatus(request.getStatus().trim());
        existing.setRemark(trimToNull(request.getRemark()));
        sysDictMapper.update(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteType(Long id) {
        SysDict existing = requireDict(id, "dict type not found");
        if (!ROOT_PARENT_VALUE.equals(existing.getParentId())) {
            throw new BusinessException("current record is not a dict type");
        }
        sysDictMapper.deleteByParentId(id);
        sysDictMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createItem(DictItemSaveRequest request) {
        ensureTypeExists(request.getParentId());
        validateCodeUnique(request.getParentId(), request.getCode(), null);
        SysDict entity = new SysDict();
        entity.setParentId(request.getParentId());
        entity.setDictCode(request.getCode().trim());
        entity.setDictName(request.getName().trim());
        entity.setSortNum(nextSortNum(request.getParentId()));
        entity.setStatus(request.getStatus().trim());
        entity.setRemark(trimToNull(request.getRemark()));
        sysDictMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(DictItemSaveRequest request) {
        SysDict existing = requireDict(request.getId(), "dict item not found");
        if (ROOT_PARENT_VALUE.equals(existing.getParentId())) {
            throw new BusinessException("current record is not a dict item");
        }
        ensureTypeExists(request.getParentId());
        validateCodeUnique(request.getParentId(), request.getCode(), request.getId());
        existing.setParentId(request.getParentId());
        existing.setDictCode(request.getCode().trim());
        existing.setDictName(request.getName().trim());
        existing.setStatus(request.getStatus().trim());
        existing.setRemark(trimToNull(request.getRemark()));
        sysDictMapper.update(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        SysDict existing = requireDict(id, "dict item not found");
        if (ROOT_PARENT_VALUE.equals(existing.getParentId())) {
            throw new BusinessException("current record is not a dict item");
        }
        sysDictMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortTypes(DictSortRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            throw new BusinessException("sort ids can not be empty");
        }
        for (int index = 0; index < request.getIds().size(); index++) {
            Long id = request.getIds().get(index);
            SysDict existing = requireDict(id, "dict type not found");
            if (!ROOT_PARENT_VALUE.equals(existing.getParentId())) {
                throw new BusinessException("sort list contains non-type records");
            }
            sysDictMapper.updateSort(id, (index + 1) * 10);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortItems(DictSortRequest request) {
        ensureTypeExists(request.getParentId());
        if (CollectionUtils.isEmpty(request.getIds())) {
            throw new BusinessException("sort ids can not be empty");
        }
        for (int index = 0; index < request.getIds().size(); index++) {
            Long id = request.getIds().get(index);
            SysDict existing = requireDict(id, "dict item not found");
            if (!request.getParentId().equals(existing.getParentId())) {
                throw new BusinessException("sort list contains records from another type");
            }
            sysDictMapper.updateSort(id, (index + 1) * 10);
        }
    }

    @Override
    public List<DictTreeVO> listEnabledTree(String code) {
        List<SysDict> typeList = sysDictMapper.selectEnabledTypes(code);
        List<DictTreeVO> result = new ArrayList<>(typeList.size());
        for (SysDict type : typeList) {
            DictTreeVO typeVO = toTreeVO(type);
            List<DictTreeVO> children = sysDictMapper.selectEnabledItems(type.getId())
                    .stream()
                    .map(this::toTreeVO)
                    .toList();
            typeVO.setChildren(children);
            result.add(typeVO);
        }
        return result;
    }

    @Override
    public List<DictTypeVO> listEnabledTypes(String code) {
        return sysDictMapper.selectEnabledTypes(code)
                .stream()
                .map(this::toTypeVO)
                .toList();
    }

    @Override
    public List<DictItemVO> listEnabledItems(Long parentId, String typeCode) {
        if (parentId == null && !StringUtils.hasText(typeCode)) {
            throw new BusinessException("parentId and typeCode can not both be empty");
        }
        List<SysDict> entityList = parentId != null
                ? sysDictMapper.selectEnabledItems(parentId)
                : sysDictMapper.selectEnabledItemsByTypeCode(typeCode);
        return entityList.stream()
                .map(this::toItemVO)
                .toList();
    }

    @Override
    public List<DictItemVO> listEnabledItems(DictCodeEnum dictCodeEnum) {
        if (dictCodeEnum == null) {
            throw new BusinessException("dict code can not be null");
        }
        return listEnabledItems(null, dictCodeEnum.getCode());
    }

    private void validateCodeUnique(Long parentId, String code, Long excludeId) {
        Integer count = sysDictMapper.countByParentIdAndCode(parentId, code.trim(), excludeId);
        if (count != null && count > 0) {
            throw new BusinessException("dict code can not be duplicated under same parent");
        }
    }

    private void ensureTypeExists(Long typeId) {
        SysDict type = requireDict(typeId, "dict type not found");
        if (!ROOT_PARENT_VALUE.equals(type.getParentId())) {
            throw new BusinessException("parentId must point to a dict type");
        }
    }

    private SysDict requireDict(Long id, String message) {
        SysDict entity = sysDictMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(message);
        }
        return entity;
    }

    private Integer nextSortNum(Long parentId) {
        Integer maxSort = sysDictMapper.selectMaxSortNumByParentId(parentId);
        return (maxSort == null ? 0 : maxSort) + 1;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private DictTypeVO toTypeVO(SysDict entity) {
        DictTypeVO vo = new DictTypeVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getDictCode());
        vo.setName(entity.getDictName());
        vo.setSort(entity.getSortNum());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private DictItemVO toItemVO(SysDict entity) {
        DictItemVO vo = new DictItemVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setCode(entity.getDictCode());
        vo.setName(entity.getDictName());
        vo.setSort(entity.getSortNum());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private DictTreeVO toTreeVO(SysDict entity) {
        DictTreeVO vo = new DictTreeVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setCode(entity.getDictCode());
        vo.setName(entity.getDictName());
        vo.setSort(entity.getSortNum());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }
}
