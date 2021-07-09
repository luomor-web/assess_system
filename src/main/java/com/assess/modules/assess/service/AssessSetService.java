package com.assess.modules.assess.service;

import com.assess.common.utils.PageUtils;
import com.assess.modules.assess.entity.AssessSet;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 数据字典
 *
 * @author
 */
public interface AssessSetService extends IService<AssessSet> {

    PageUtils queryPage(Map<String, Object> params);

    void deleteBatch(Long[] ids);

    void update(AssessSet record);

    AssessSet get(Long id);
}
