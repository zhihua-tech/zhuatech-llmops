/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.llmops.workbench;
import org.springframework.stereotype.Service;
import java.math.*;
import java.util.*;
import static cn.zhuatech.llmops.workbench.WorkbenchInput.*;
/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@Service
public class AiWorkbenchService {
    /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
    public Map<String,Object> execute(Map<String,Object> r){
        validate(r);

        keys(r,"baselineRequests","baselineErrors","canaryRequests","canaryErrors","minSamples","maxErrorRate","canaryP95Ms","maxP95Ms");int bn=integer(r,"baselineRequests",1,100000000),be=integer(r,"baselineErrors",0,bn),cn=integer(r,"canaryRequests",1,100000000),ce=integer(r,"canaryErrors",0,cn),minimum=integer(r,"minSamples",1,100000000);double threshold=decimal(r,"maxErrorRate",0,1).doubleValue();int latency=integer(r,"canaryP95Ms",0,3600000),limit=integer(r,"maxP95Ms",1,3600000);
        double z=1.959963984540054,z2=z*z,bp=(double)be/bn,cp=(double)ce/cn;double bc=(bp+z2/(2*bn))/(1+z2/bn),bh=z*Math.sqrt(bp*(1-bp)/bn+z2/(4.0*bn*bn))/(1+z2/bn);double cc=(cp+z2/(2*cn))/(1+z2/cn),ch=z*Math.sqrt(cp*(1-cp)/cn+z2/(4.0*cn*cn))/(1+z2/cn);
        String recommendation=latency>limit||cc-ch>threshold?"ROLLBACK":bn<minimum||cn<minimum?"OBSERVE":cc+ch<=threshold&&cc-ch<=bc+bh?"ELIGIBLE_FOR_REVIEW":"OBSERVE";
        return Map.of("recommendation",recommendation,"baselineErrorRate",round(bp),"canaryErrorRate",round(cp),"baselineInterval",List.of(round(Math.max(0,bc-bh)),round(Math.min(1,bc+bh))),"canaryInterval",List.of(round(Math.max(0,cc-ch)),round(Math.min(1,cc+ch))),"confidence",0.95,"changesTraffic",false);

    }
}
