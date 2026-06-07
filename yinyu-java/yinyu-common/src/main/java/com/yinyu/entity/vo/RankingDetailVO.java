package com.yinyu.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class RankingDetailVO {

    private RankingBoardVO currentBoard;

    private List<RankingBoardVO> boards;

    private List<SongVO> songs;
}
