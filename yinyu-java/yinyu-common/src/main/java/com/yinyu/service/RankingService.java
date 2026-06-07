package com.yinyu.service;

import com.yinyu.entity.vo.RankingBoardVO;
import com.yinyu.entity.vo.RankingDetailVO;

import java.util.List;

public interface RankingService {

    List<RankingBoardVO> listBoards();

    RankingDetailVO getDetail(String boardCode);
}
