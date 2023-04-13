package misaka.data.bank.support.supfree;

import artoria.action.ActionUtils;
import misaka.data.bank.BankCardQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/**
 * Supfree net.
 * @author Kahle
 */
@Configuration
public class SupfreeNetAutoConfiguration implements InitializingBean, DisposableBean {
    private static Logger log = LoggerFactory.getLogger(SupfreeNetAutoConfiguration.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        SupfreeBankCardActionHandler handler = new SupfreeBankCardActionHandler();
        String actionName = "bank-card-supfree";
        ActionUtils.registerHandler(actionName, handler);
        ActionUtils.registerHandler("class:" + BankCardQuery.class.getName(), handler);
    }

    @Override
    public void destroy() throws Exception {
    }

}