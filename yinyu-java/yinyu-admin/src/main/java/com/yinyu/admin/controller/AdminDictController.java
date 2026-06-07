package com.yinyu.admin.controller;

import com.yinyu.api.ApiResponse;
import com.yinyu.api.ListData;
import com.yinyu.entity.dto.*;
import com.yinyu.entity.enums.DictCodeEnum;
import com.yinyu.entity.vo.DictItemVO;
import com.yinyu.entity.vo.DictTypeVO;
import com.yinyu.service.DictService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/admin/dicts")
public class AdminDictController {

    private final DictService dictService;

    public AdminDictController(DictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("/types")
    public ApiResponse<ListData<DictTypeVO>> listTypes(@Valid DictTypeQueryRequest request) {
        List<DictTypeVO> list = dictService.listTypeList(request);
        return ApiResponse.success(new ListData<>(list, (long) list.size()));
    }

    @GetMapping("/items")
    public ApiResponse<ListData<DictItemVO>> listItems(@Valid DictItemQueryRequest request) {
        List<DictItemVO> list = dictService.listItemList(request);
        return ApiResponse.success(new ListData<>(list, (long) list.size()));
    }

    @GetMapping("/items/options")
    public ApiResponse<ListData<DictItemVO>> listEnabledItemsByCode(@RequestParam String code) {
        List<DictItemVO> list = dictService.listEnabledItems(DictCodeEnum.fromCode(code));
        return ApiResponse.success(new ListData<>(list, (long) list.size()));
    }

    @PostMapping("/type")
    public ApiResponse<Void> createType(@RequestBody @Valid DictTypeSaveRequest request) {
        dictService.createType(request);
        return ApiResponse.success("dict type created", null);
    }

    @PutMapping("/type")
    public ApiResponse<Void> updateType(@RequestBody @Valid DictTypeSaveRequest request) {
        dictService.updateType(request);
        return ApiResponse.success("dict type updated", null);
    }

    @DeleteMapping("/type/{id}")
    public ApiResponse<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return ApiResponse.success("dict type deleted", null);
    }

    @PostMapping("/item")
    public ApiResponse<Void> createItem(@RequestBody @Valid DictItemSaveRequest request) {
        dictService.createItem(request);
        return ApiResponse.success("dict item created", null);
    }

    @PutMapping("/item")
    public ApiResponse<Void> updateItem(@RequestBody @Valid DictItemSaveRequest request) {
        dictService.updateItem(request);
        return ApiResponse.success("dict item updated", null);
    }

    @DeleteMapping("/item/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        dictService.deleteItem(id);
        return ApiResponse.success("dict item deleted", null);
    }

    @PutMapping("/types/sort")
    public ApiResponse<Void> sortTypes(@RequestBody @Valid DictSortRequest request) {
        dictService.sortTypes(request);
        return ApiResponse.success("dict types sorted", null);
    }

    @PutMapping("/items/sort")
    public ApiResponse<Void> sortItems(@RequestBody @Valid DictSortRequest request) {
        dictService.sortItems(request);
        return ApiResponse.success("dict items sorted", null);
    }
}
