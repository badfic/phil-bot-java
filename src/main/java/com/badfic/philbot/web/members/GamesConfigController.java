package com.badfic.philbot.web.members;

import com.badfic.philbot.config.ControllerConfigurable;
import com.badfic.philbot.data.SwampyGamesConfig;
import com.badfic.philbot.data.SwampyGamesConfigDal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class GamesConfigController extends BaseMembersController {

    private final SwampyGamesConfigDal swampyGamesConfigDal;
    private final ObjectMapper objectMapper;

    @GetMapping(value = "/games-config", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, true);

        final var swampyGamesConfig = swampyGamesConfigDal.get();

        final var configEntries = new LinkedMultiValueMap<ControllerConfigurable.Category, ConfigEntryGet>();

        for (final Field declaredField : SwampyGamesConfig.class.getDeclaredFields()) {
            final var annotation = declaredField.getAnnotation(ControllerConfigurable.class);
            if (annotation != null) {
                declaredField.setAccessible(true);

                switch (annotation.type()) {
                    case INT, LONG, STRING ->
                            configEntries.add(annotation.category(), new ConfigEntryGet(declaredField.getName(), declaredField.get(swampyGamesConfig).toString(), false, false));
                    case IMG ->
                            configEntries.add(annotation.category(), new ConfigEntryGet(declaredField.getName(), declaredField.get(swampyGamesConfig).toString(), true, false));
                    case STRING_SET ->
                            configEntries.add(annotation.category(), new ConfigEntryGet(declaredField.getName(), objectMapper.writeValueAsString(declaredField.get(swampyGamesConfig)), false, true));
                    default -> throw new IllegalStateException();
                }
            }
        }

        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Games Config");
        addCommonProps(httpServletRequest.getSession(), props);
        props.put("configEntries", configEntries.entrySet());
        props.put("categories", ControllerConfigurable.Category.values());

        return new ModelAndView("games-config", props);
    }

    @PostMapping(value = "/games-config", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> post(final HttpServletRequest httpServletRequest, final @RequestBody ConfigEntryPut configEntry) throws Exception {
        checkSession(httpServletRequest, true);

        if (configEntry != null && StringUtils.isNotBlank(configEntry.fieldName()) && StringUtils.isNotBlank(configEntry.fieldValue())) {
            final var declaredField = SwampyGamesConfig.class.getDeclaredField(configEntry.fieldName());
            declaredField.setAccessible(true);

            final var controllerConfigurableAnnotation = declaredField.getAnnotation(ControllerConfigurable.class);

            if (controllerConfigurableAnnotation == null) {
                return ResponseEntity.badRequest().build();
            }

            final var swampyGamesConfig = swampyGamesConfigDal.get();

            switch (controllerConfigurableAnnotation.type()) {
                case INT -> {
                    final var realValue = Integer.parseInt(configEntry.fieldValue());
                    declaredField.set(swampyGamesConfig, realValue);
                }
                case LONG -> {
                    final var realValue = Long.parseLong(configEntry.fieldValue());
                    declaredField.set(swampyGamesConfig, realValue);
                }
                case STRING, IMG -> declaredField.set(swampyGamesConfig, configEntry.fieldValue());
                case STRING_SET -> {
                    final var fieldValueRaw = configEntry.fieldValue();

                    final var fieldValue = objectMapper.readValue(fieldValueRaw, String[].class);

                    declaredField.set(swampyGamesConfig, fieldValue);
                }
            }

            swampyGamesConfigDal.update(swampyGamesConfig);
        }

        return ResponseEntity.ok("Saved. If it was an image you'll have to refresh to see the new image.");
    }

    private record ConfigEntryGet(String fieldName, String fieldValue, Boolean valueIsImg, Boolean valueIsSet) {}

    private record ConfigEntryPut(String fieldName, String fieldValue) {}

}
