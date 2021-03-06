/**
 *
 */

package com.assess.modules.assess.controller;


import com.assess.common.annotation.SysLog;
import com.assess.common.utils.Constant;
import com.assess.common.utils.DateUtils;
import com.assess.common.utils.PageUtils;
import com.assess.common.utils.R;
import com.assess.common.validator.ValidatorUtils;
import com.assess.modules.assess.constant.DeclareStatusEnum;
import com.assess.modules.assess.entity.AssessApproval;
import com.assess.modules.assess.entity.AssessDeclare;
import com.assess.modules.assess.service.AssessApprovalService;
import com.assess.modules.assess.service.AssessDeclareService;
import com.assess.modules.sys.controller.AbstractController;
import com.assess.modules.sys.entity.SysUser;
import com.assess.modules.sys.service.SysUserRoleService;
import com.assess.modules.sys.service.SysUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/assess/declare")
public class AssessDeclareController extends AbstractController {
    @Autowired
    private AssessDeclareService declareService;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    /**
     * ๆๆๅ่กจ
     */
    @RequestMapping("/list")
    @RequiresPermissions("assess:declare:list")
    public R list(@RequestParam Map<String, Object> params) {
        Long userId = getUserId();
        List<Long> roleIdList = sysUserRoleService.queryRoleIdList(userId);
        if (userId == Constant.SUPER_ADMIN) {
            userId = null;
        }else {
            for (Long roleId : roleIdList) {
                if (roleId == 2 || roleId == 1) {
                    userId = null;
                    break;
                }
            }
        }
        params.put("userId", userId);
        PageUtils page = declareService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * ่ฏฆๆ
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("assess:declare:info")
    @ResponseBody
    public R info(@PathVariable("id") Long id) {
        AssessDeclare declare = declareService.get(id);

        return R.ok().put("declare", declare);
    }

    /**
     * ไฟๅญ
     */
    @SysLog("ไฟๅญ")
    @RequestMapping("/save")
    @RequiresPermissions("assess:declare:save")
    public R save(@RequestBody AssessDeclare declare) {
        ValidatorUtils.validateEntity(declare);
        declare.setUpdateTime(DateUtils.currentTimeFormat());
        if (declare.getStatus() != DeclareStatusEnum.WAIT.getCode()) {
            return R.error("ๅทฒ็ณๆฅๆ?้้ๅค็ณๆฅ");
        }
        //่ชๅทฑ็ณๆฅ่ชๅทฑ็
        if(declare.getUserId()!=getUserId()){
            return R.error("ๅช่ฝ่ชๅทฑ็ณๆฅ่ชๅทฑ็็ปฉๆ่ๆ?ธ");
        }
        //ๅฐๆฏไธไธช่ๆ?ธ้กนๆๅฅๆฐๆฎๅบ
        List<AssessDeclare> declareList = declare.getDeclareList();
        Integer score = 0;
        for (AssessDeclare d : declareList) {
            score += d.getScore();
        }
        declareService.saveBatch(declareList);
        declare.setStatus(DeclareStatusEnum.NO_APPROVAL.getCode());
        declare.setScore(score);
        declareService.update(declare);
        return R.ok();
    }

    /**
     * ไฟฎๆน
     */
    @SysLog("ไฟฎๆน")
    @RequestMapping("/update")
    @RequiresPermissions("assess:declare:update")
    public R update(@RequestBody AssessDeclare declare) {
        ValidatorUtils.validateEntity(declare);
        declare.setUpdateTime(DateUtils.currentTimeFormat());
        if (declare.getStatus() != DeclareStatusEnum.WAIT.getCode()) {
            return R.error("ๅทฒ็ณๆฅๆ?้้ๅค็ณๆฅ");
        }
        //ๅฐๆฏไธไธช่ๆ?ธ้กนๆๅฅๆฐๆฎๅบ
        List<AssessDeclare> declareList = declare.getDeclareList();
        Integer score = 0;
        for (AssessDeclare d : declareList) {
            score += d.getScore();
        }
        declareService.saveBatch(declareList);
        declare.setStatus(DeclareStatusEnum.NO_APPROVAL.getCode());
        declare.setScore(score);
        declareService.update(declare);
        return R.ok();
    }

    /**
     * ๅ?้ค
     */
    @SysLog("ๅ?้ค")
    @RequestMapping("/delete")
    @RequiresPermissions("assess:declare:delete")
    public R delete(@RequestBody Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            Long id = ids[i];
            AssessDeclare assessDeclare = declareService.get(id);
            if(assessDeclare.getStatus()!=DeclareStatusEnum.NO_APPROVAL.getCode()){
                return R.error("็ฌฌ" + (i + 1) + "ๆกไฟกๆฏ๏ผๅฝๅ็ถๆไธๅฏๅ?้ค");
            }
            //1.ๆดๆฐ็ณๆฅ็ถๆ
            AssessDeclare declare = new AssessDeclare();
            declare.setId(id);
            declare.setStatus(DeclareStatusEnum.WAIT.getCode());
            declare.setScore(0);
            declareService.update(declare);
            //2.ๅ?้ค็ณๆฅ่ฎฐๅฝ
            List<AssessDeclare> declareList = assessDeclare.getDeclareList();
            Long[] declareIds =new Long[declareList.size()];
            for (int x = 0; x < declareList.size(); x++) {
                AssessDeclare d = declareList.get(x);
                declareIds[x]=d.getId();
            }
            declareService.deleteBatch(declareIds);
        }
        return R.ok();
    }

}