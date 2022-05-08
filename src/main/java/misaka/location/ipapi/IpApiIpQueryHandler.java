package misaka.location.ipapi;

import artoria.beans.BeanUtils;
import artoria.exchange.JsonUtils;
import artoria.net.HttpUtils;
import artoria.query.AbstractQueryHandler;
import artoria.util.MapUtils;
import artoria.util.StringUtils;
import artoria.util.TypeUtils;
import misaka.location.ip.IpLocation;
import misaka.location.ip.IpQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Network physical address provider by website(http://ip-api.com).
 * @see <a href="http://ip-api.com/">IP Geolocation API</a>
 * @author Kahle
 */
public class IpApiIpQueryHandler extends AbstractQueryHandler {
    private static Logger log = LoggerFactory.getLogger(IpApiIpQueryHandler.class);
    private Class<?>[] supportClasses = new Class[] { IpApiIpLocation.class, IpLocation.class};

    @Override
    public <T> T info(Map<?, ?> properties, Object input, Class<T> clazz) {
        isSupport(supportClasses, clazz);
        IpQuery ipQuery = (IpQuery) input;
        String ipAddress = ipQuery.getIpAddress();
        String language = ipQuery.getLanguage();
        if (StringUtils.isBlank(language)) { language = "zh-CN"; }
        String jsonString = HttpUtils.get("http://ip-api.com/json/" + ipAddress + "?lang=" + language);
        if (StringUtils.isBlank(jsonString)) { return null; }
        ParameterizedType type = TypeUtils.parameterizedOf(Map.class, String.class, String.class);
        Map<String, String> map = JsonUtils.parseObject(jsonString, type);
        if (MapUtils.isEmpty(map)) { return null; }
        IpApiIpLocation ipApiIpLocation = new IpApiIpLocation();
        ipApiIpLocation.setIpAddress(ipAddress);
        ipApiIpLocation.setCountry(map.get("country"));
        ipApiIpLocation.setCountryCode(map.get("countryCode"));
        ipApiIpLocation.setRegion(map.get("regionName"));
        ipApiIpLocation.setRegionCode(map.get("region"));
        ipApiIpLocation.setCity(map.get("city"));
        ipApiIpLocation.setCityCode(null);
        ipApiIpLocation.setIsp(map.get("isp"));
        ipApiIpLocation.setOrg(map.get("org"));
        ipApiIpLocation.setTimezone(map.get("timezone"));
        ipApiIpLocation.setZip(map.get("zip"));
        ipApiIpLocation.setAs(map.get("as"));
        String lat = map.get("lat");
        String lon = map.get("lon");
        try {
            BigDecimal latitude = lat != null ? new BigDecimal(lat) : null;
            BigDecimal longitude = lon != null ? new BigDecimal(lon) : null;
            ipApiIpLocation.setLatitude(latitude);
            ipApiIpLocation.setLongitude(longitude);
        }
        catch (Exception e) {
            log.info("Parse latitude and longitude to double error", e);
        }
        return BeanUtils.beanToBean(ipApiIpLocation, clazz);
    }

}