package com.badfic.philbot.web.members;

import com.badfic.philbot.data.ReminderDal;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class RemindersController extends BaseMembersController {

    private final ReminderDal reminderDal;

    @GetMapping(value = "/reminders", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(final HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);
        final var guild = philJda.getGuildById(baseConfig.guildId);

        final var simpleReminderList = reminderDal.findAll()
                .stream()
                .map(r -> {
                    final var userId = r.getUserId();
                    final var memberById = guild.getMemberById(userId);

                    return new SimpleReminder(r.getId(), memberById != null ? memberById.getEffectiveName() : Long.toString(userId), r.getReminder(),
                            r.getDueDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                }).toList();

        final var props = new HashMap<String, Object>();
        props.put("pageTitle", "Reminders");
        addCommonProps(httpServletRequest.getSession(), props);
        props.put("reminders", simpleReminderList);

        return new ModelAndView("reminders", props);
    }

    private record SimpleReminder(long id, String name, String reminder, long dueDate) {}

}
