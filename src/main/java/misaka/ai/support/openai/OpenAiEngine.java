package misaka.ai.support.openai;

import artoria.ai.support.AbstractClassicAiEngine;
import artoria.data.Dict;
import artoria.data.bean.BeanUtils;
import artoria.data.json.JsonUtils;
import artoria.util.Assert;
import artoria.util.StringUtils;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.Proxy;
import java.util.List;
import java.util.Map;

import static artoria.common.constant.Numbers.ZERO;

/**
 * The ai engine based on the openai api.
 * @see <a href="https://platform.openai.com/docs/api-reference">API REFERENCE</>
 * @author Kahle
 */
public class OpenAiEngine extends AbstractClassicAiEngine {
    private static final Logger log = LoggerFactory.getLogger(OpenAiEngine.class);
    protected static final String STREAM_KEY = "stream";
    protected static final String PROMPT_KEY = "prompt";
    protected static final String ERROR_KEY  = "error";
    protected static final String MODEL_KEY  = "model";
    private final String apiKey;
    private Proxy proxy;

    public OpenAiEngine(String apiKey) {
        Assert.notBlank(apiKey, "Parameter \"apiKey\" must not blank. ");
        this.apiKey = apiKey;
    }

    public Proxy getProxy() {

        return proxy;
    }

    public void setProxy(Proxy proxy) {

        this.proxy = proxy;
    }

    protected String getApiKey() {

        return apiKey;
    }

    protected Dict convertToDict(Object input) {
        if (input instanceof Dict) {
            return (Dict) input;
        }
        else if (input instanceof Map) {
            return Dict.of((Map<?, ?>) input);
        }
        else {
            return Dict.of(BeanUtils.beanToMap(input));
        }
    }

    protected Object post(String url, String body, Boolean stream) {
        if (stream == null) { stream = false; }
        HttpRequest request = HttpRequest.post(url)
                .header("Authorization", "Bearer " + getApiKey())
                .body(body);
        if (getProxy() != null) {
            request.setProxy(getProxy());
        }
        if (stream) {
            HttpResponse response = request.executeAsync();
            return response.bodyStream();
        }
        else {
            return request.execute().body();
        }
    }

    protected void checkResult(Map<?, ?> map) {
        Dict result = map instanceof Dict ? (Dict) map : Dict.of(map);
        Object errorObj = result.get(ERROR_KEY);
        if (errorObj == null) { return; }
        Dict error = Dict.of(errorObj instanceof Map
                ? (Map<?, ?>) errorObj : BeanUtils.beanToMap(errorObj));
        if (MapUtil.isEmpty(error)) { return; }
        String message = error.getString("message");
        String code = error.getString("code");
        Assert.state(StrUtil.isBlank(code), message);
    }

    @Override
    public Object execute(Object input, String strategy, Class<?> clazz) {
        if ("chat".equals(strategy)) {
            return chat(input, strategy, clazz);
        }
        if ("embedding".equals(strategy)) {
            return embedding(input, strategy, clazz);
        }
        else {
            return completion(input, strategy, clazz);
        }
    }

    protected Object completion(Object input, String strategy, Class<?> clazz) {
        Assert.notNull(input, "Parameter \"input\" must not null. ");
        // data conversion.
        Dict data;
        if (input instanceof String) {
            data = Dict.of(PROMPT_KEY, input);
        }
        else { data = convertToDict(input); }
        // parameters validation and default value handle.
        boolean stream = data.getBoolean(STREAM_KEY, Boolean.FALSE);
        String prompt = data.getString(PROMPT_KEY);
        String model = data.getString(MODEL_KEY);
        Assert.notBlank(prompt, "Parameter \"prompt\" must not blank. ");
        if (StringUtils.isBlank(model)) { data.set(MODEL_KEY, "text-davinci-003"); }
        // Parameter clazz validation
        if (stream) { isSupport(new Class[]{ InputStream.class }, clazz); }
        else { isSupport(new Class[]{ CharSequence.class, Map.class }, clazz); }
        // open ai api invoke.
        String json = JsonUtils.toJsonString(data);
        log.debug("The openai completions api request \"{}\". ", json);
        Object response = post("https://api.openai.com/v1/completions", json, stream);
        // response is InputStream.
        if (stream) { return response; }
        // response is string
        String body = String.valueOf(response);
        log.debug("The openai completions api response \"{}\". ", body);
        // response parse
        Dict result = JsonUtils.parseObject(body, Dict.class);
        // result check
        checkResult(result);
        // convert result
        if (CharSequence.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("rawtypes")
            List choices = (List) result.get("choices");
            @SuppressWarnings("rawtypes")
            Map first = choices != null && !choices.isEmpty() ? (Map) choices.get(ZERO) : null;
            String text = first != null ? (String) first.get("text") : null;
            Assert.notBlank(text, "The openai completions api first choice text is blank. ");
            return text;
        }
        else { return Dict.of(result); }
    }

    protected Object chat(Object input, String strategy, Class<?> clazz) {

        throw new UnsupportedOperationException();
    }

    protected Object embedding(Object input, String strategy, Class<?> clazz) {

        throw new UnsupportedOperationException();
    }

}