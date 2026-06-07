package com.yinyu.web.controller;

import com.yinyu.api.ApiResponse;
import com.yinyu.api.ListData;
import com.yinyu.entity.vo.DictItemVO;
import com.yinyu.entity.vo.DictTreeVO;
import com.yinyu.entity.vo.DictTypeVO;
import com.yinyu.service.DictService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;

@Validated
@RestController
@RequestMapping("/web/dicts")
public class WebDictController {

    private final DictService dictService;

    public WebDictController(DictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("/types")
    public ApiResponse<ListData<DictTypeVO>> listTypes(@RequestParam(required = false) String code) {
        List<DictTypeVO> list = dictService.listEnabledTypes(code);
        return ApiResponse.success(new ListData<>(list, (long) list.size()));
    }

    @GetMapping("/items")
    public ApiResponse<ListData<DictItemVO>> listItems(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String typeCode
    ) {
        List<DictItemVO> list = dictService.listEnabledItems(parentId, typeCode);
        return ApiResponse.success(new ListData<>(list, (long) list.size()));
    }

    @GetMapping("/tree")
    public ApiResponse<List<DictTreeVO>> tree(@RequestParam(required = false) String code) {
        return ApiResponse.success(dictService.listEnabledTree(code));
    }

    public static void main(String[] args) {
        File file = new File("E:\\歌曲");
        File[] listFile = file.listFiles();
        for (File file1 : listFile) {
            System.out.println(file1.getName());
        }
    }
}
