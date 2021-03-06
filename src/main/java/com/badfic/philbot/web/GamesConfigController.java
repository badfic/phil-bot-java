package com.badfic.philbot.web;

import com.badfic.philbot.config.ControllerConfigurable;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.data.phil.SwampyGamesConfigRepository;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class GamesConfigController extends BaseController {

    @Resource
    private SwampyGamesConfigRepository swampyGamesConfigRepository;

    @GetMapping(value = "/games-config", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        SwampyGamesConfig swampyGamesConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID).orElseThrow(IllegalStateException::new);

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Games Config");
        props.put("username", httpServletRequest.getSession().getAttribute(DISCORD_USERNAME));
        props.put("isMod", httpServletRequest.getSession().getAttribute(DISCORD_IS_MOD));

        List<ConfigEntry> configEntries = new ArrayList<>();

        for (Field declaredField : SwampyGamesConfig.class.getDeclaredFields()) {
            ControllerConfigurable annotation = declaredField.getAnnotation(ControllerConfigurable.class);
            if (annotation != null) {
                declaredField.setAccessible(true);
                configEntries.add(new ConfigEntry(declaredField.getName(), declaredField.get(swampyGamesConfig).toString(),
                        annotation.type() == ControllerConfigurable.Type.IMG));
            }
        }

        props.put("configEntries", configEntries);

        return new ModelAndView("games-config", props);
    }

    @PostMapping(value = "/games-config", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> post(HttpServletRequest httpServletRequest, @RequestBody ConfigEntry configEntry) throws Exception {
        checkSession(httpServletRequest, true);

        SwampyGamesConfig swampyGamesConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID).orElseThrow(IllegalStateException::new);

        if (configEntry != null && StringUtils.isNotBlank(configEntry.getFieldName()) && StringUtils.isNotBlank(configEntry.getFieldValue())) {
            Field declaredField = SwampyGamesConfig.class.getDeclaredField(configEntry.getFieldName());
            declaredField.setAccessible(true);

            if (int.class.equals(declaredField.getType())) {
                int realValue = Integer.parseInt(configEntry.getFieldValue());

                declaredField.set(swampyGamesConfig, realValue);
            } else {
                declaredField.set(swampyGamesConfig, configEntry.getFieldValue());
            }

            swampyGamesConfigRepository.save(swampyGamesConfig);
        }

        return ResponseEntity.ok("Saved. If it was an image you'll have to refresh to see the new image.");
    }

    private static class ConfigEntry {
        private String fieldName;
        private String fieldValue;
        private Boolean valueIsImg;

        public ConfigEntry() {
        }

        public ConfigEntry(String fieldName, String fieldValue, Boolean valueIsImg) {
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
            this.valueIsImg = valueIsImg;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldValue() {
            return fieldValue;
        }

        public void setFieldValue(String fieldValue) {
            this.fieldValue = fieldValue;
        }

        public Boolean getValueIsImg() {
            return valueIsImg;
        }

        public void setValueIsImg(Boolean valueIsImg) {
            this.valueIsImg = valueIsImg;
        }
    }

}
