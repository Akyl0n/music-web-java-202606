package com.yinyu.service.impl;

import com.yinyu.api.ListData;
import com.yinyu.entity.dto.SingerQueryRequest;
import com.yinyu.entity.dto.SingerSaveRequest;
import com.yinyu.entity.dto.SingerStatusRequest;
import com.yinyu.entity.po.Singer;
import com.yinyu.entity.vo.SingerVO;
import com.yinyu.exception.BusinessException;
import com.yinyu.mapper.SingerMapper;
import com.yinyu.service.SingerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SingerServiceImpl implements SingerService {

    private final SingerMapper singerMapper;

    public SingerServiceImpl(SingerMapper singerMapper) {
        this.singerMapper = singerMapper;
    }

    @Override
    public ListData<SingerVO> listPage(SingerQueryRequest request) {
        SingerQueryRequest query = request == null ? new SingerQueryRequest() : request;
        List<SingerVO> list = singerMapper.selectPage(query).stream().map(this::toVO).toList();
        Long total = singerMapper.countPage(query);
        return new ListData<>(list, total == null ? 0L : total);
    }

    @Override
    public SingerVO getById(Long id) {
        return toVO(requireSinger(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SingerSaveRequest request) {
        validateNameUnique(request.getName(), null);
        Singer entity = new Singer();
        fillSinger(entity, request);
        singerMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SingerSaveRequest request) {
        if (request.getId() == null) {
            throw new BusinessException("id can not be null");
        }
        Singer existing = requireSinger(request.getId());
        validateNameUnique(request.getName(), request.getId());
        fillSinger(existing, request);
        singerMapper.update(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireSinger(id);
        singerMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(SingerStatusRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            throw new BusinessException("ids can not be empty");
        }
        request.getIds().forEach(this::requireSinger);
        singerMapper.updateStatus(request.getIds(), request.getStatus().trim());
    }

    private void fillSinger(Singer entity, SingerSaveRequest request) {
        entity.setName(request.getName().trim());
        entity.setGender(trimToNull(request.getGender()));
        entity.setBirthday(request.getBirthday());
        entity.setRegion(trimToNull(request.getRegion()));
        entity.setType(request.getType().trim());
        entity.setAvatar(trimToNull(request.getAvatar()));
        entity.setIntro(trimToNull(request.getIntro()));
        entity.setTags(normalizeTags(request.getTags()));
        entity.setLetter(normalizeLetter(request.getLetter()));
        entity.setStatus(request.getStatus().trim());
        entity.setRemark(trimToNull(request.getRemark()));
    }

    private void validateNameUnique(String name, Long excludeId) {
        Integer count = singerMapper.countByName(name.trim(), excludeId);
        if (count != null && count > 0) {
            throw new BusinessException("singer name already exists");
        }
    }

    private Singer requireSinger(Long id) {
        Singer entity = singerMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("singer not found");
        }
        return entity;
    }

    private String normalizeLetter(String letter) {
        if (!StringUtils.hasText(letter)) {
            return "#";
        }
        return letter.trim().toUpperCase();
    }

    private String normalizeTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return null;
        }
        return String.join(",", List.of(tags.split(",")).stream().map(String::trim).filter(StringUtils::hasText).distinct().toList());
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private SingerVO toVO(Singer entity) {
        SingerVO vo = new SingerVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setGender(entity.getGender());
        vo.setBirthday(entity.getBirthday());
        vo.setRegion(entity.getRegion());
        vo.setType(entity.getType());
        vo.setAvatar(entity.getAvatar());
        vo.setIntro(entity.getIntro());
        vo.setTags(entity.getTags());
        vo.setLetter(entity.getLetter());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
