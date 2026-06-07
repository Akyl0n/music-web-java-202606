package com.yinyu.service;

import com.yinyu.entity.dto.*;
import com.yinyu.entity.enums.DictCodeEnum;
import com.yinyu.entity.vo.DictItemVO;
import com.yinyu.entity.vo.DictTreeVO;
import com.yinyu.entity.vo.DictTypeVO;

import java.util.List;

public interface DictService {

    List<DictTypeVO> listTypeList(DictTypeQueryRequest request);

    List<DictItemVO> listItemList(DictItemQueryRequest request);

    void createType(DictTypeSaveRequest request);

    void updateType(DictTypeSaveRequest request);

    void deleteType(Long id);

    void createItem(DictItemSaveRequest request);

    void updateItem(DictItemSaveRequest request);

    void deleteItem(Long id);

    void sortTypes(DictSortRequest request);

    void sortItems(DictSortRequest request);

    List<DictTreeVO> listEnabledTree(String code);

    List<DictTypeVO> listEnabledTypes(String code);

    List<DictItemVO> listEnabledItems(Long parentId, String typeCode);

    List<DictItemVO> listEnabledItems(DictCodeEnum dictCodeEnum);
}
