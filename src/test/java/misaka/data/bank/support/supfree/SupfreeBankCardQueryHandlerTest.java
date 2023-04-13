package misaka.data.bank.support.supfree;

import artoria.action.ActionUtils;
import artoria.data.json.JsonUtils;
import artoria.data.json.support.FastJsonProvider;
import com.alibaba.fastjson.JSON;
import misaka.data.bank.BankCard;
import misaka.data.bank.BankCardQuery;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class SupfreeBankCardQueryHandlerTest {
    private static Logger log = LoggerFactory.getLogger(SupfreeBankCardQueryHandlerTest.class);

    @Test
    public void test1() {
        JsonUtils.setJsonProvider(new FastJsonProvider());
        ActionUtils.registerHandler(BankCardQuery.class, new SupfreeBankCardActionHandler());

        BankCardQuery cardQuery = new BankCardQuery("622600687501042806");
        BankCard bankCard = ActionUtils.execute(cardQuery, BankCard.class);
        log.info("{}", JSON.toJSONString(bankCard, true));

        cardQuery = new BankCardQuery("6230960288002899254");
        BankCard bankCard1 = ActionUtils.execute(cardQuery, BankCard.class);
        log.info("{}", JSON.toJSONString(bankCard1, true));

        cardQuery = new BankCardQuery("6217994000264606028");
        BankCard bankCard2 = ActionUtils.execute(cardQuery, BankCard.class);
        log.info("{}", JSON.toJSONString(bankCard2, true));

        cardQuery = new BankCardQuery("6230666046001759766");
        BankCard bankCard3 = ActionUtils.execute(cardQuery, BankCard.class);
        log.info("{}", JSON.toJSONString(bankCard3, true));
    }

}