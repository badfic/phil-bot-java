package com.badfic.philbot.web.members;

import com.badfic.philbot.config.ControllerConfigurable;
import com.badfic.philbot.data.phil.SwampyGamesConfig;
import com.badfic.philbot.data.phil.SwampyGamesConfigRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class GamesConfigController extends BaseMembersController {

    @Autowired
    private SwampyGamesConfigRepository swampyGamesConfigRepository;

    @GetMapping(value = "/games-config", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        SwampyGamesConfig swampyGamesConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID).orElseThrow(IllegalStateException::new);


        List<ConfigEntry> configEntries = new ArrayList<>();

        for (Field declaredField : SwampyGamesConfig.class.getDeclaredFields()) {
            ControllerConfigurable annotation = declaredField.getAnnotation(ControllerConfigurable.class);
            if (annotation != null) {
                declaredField.setAccessible(true);
                configEntries.add(new ConfigEntry(declaredField.getName(), declaredField.get(swampyGamesConfig).toString(),
                        annotation.type() == ControllerConfigurable.Type.IMG));
            }
        }

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Games Config");
        addCommonProps(httpServletRequest, props);
        props.put("configEntries", configEntries);

        return new ModelAndView("games-config", props);
    }

    @PostMapping(value = "/games-config", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> post(HttpServletRequest httpServletRequest, @RequestBody ConfigEntry configEntry) throws Exception {
        checkSession(httpServletRequest, true);

        SwampyGamesConfig swampyGamesConfig = swampyGamesConfigRepository.findById(SwampyGamesConfig.SINGLETON_ID).orElseThrow(IllegalStateException::new);

        if (configEntry != null && StringUtils.isNotBlank(configEntry.fieldName()) && StringUtils.isNotBlank(configEntry.fieldValue())) {
            Field declaredField = SwampyGamesConfig.class.getDeclaredField(configEntry.fieldName());
            declaredField.setAccessible(true);

            if (int.class.equals(declaredField.getType())) {
                int realValue = Integer.parseInt(configEntry.fieldValue());

                declaredField.set(swampyGamesConfig, realValue);
            } else if (long.class.equals(declaredField.getType())) {
                long realValue = Long.parseLong(configEntry.fieldValue());

                declaredField.set(swampyGamesConfig, realValue);
            } else {
                declaredField.set(swampyGamesConfig, configEntry.fieldValue());
            }

            swampyGamesConfigRepository.save(swampyGamesConfig);
        }

        return ResponseEntity.ok("Saved. If it was an image you'll have to refresh to see the new image.");
    }

    private record ConfigEntry(String fieldName, String fieldValue, Boolean valueIsImg) {}

}
