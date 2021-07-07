package com.badfic.philbot.web;

import com.badfic.philbot.data.phil.ReminderRepository;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class RemindersController extends BaseController {

    @Resource
    private ReminderRepository reminderRepository;

    @GetMapping(value = "/reminders", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);

        List<SimpleReminder> simpleReminderList = reminderRepository.findAll()
                .stream()
                .map(r -> {
                    long userId = r.getUserId();
                    Member memberById = philJda.getGuilds().get(0).getMemberById(userId);

                    return new SimpleReminder(r.getId(), memberById != null ? memberById.getEffectiveName() : Long.toString(userId), r.getReminder(),
                            r.getDueDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                }).collect(Collectors.toList());

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Reminders");
        props.put("username", httpServletRequest.getSession().getAttribute(DISCORD_USERNAME));
        props.put("isMod", httpServletRequest.getSession().getAttribute(DISCORD_IS_MOD));
        props.put("reminders", simpleReminderList);

        return new ModelAndView("reminders", props);
    }

    private static class SimpleReminder {
        private final long id;
        private final String name;
        private final String reminder;
        private final long dueDate;

        public SimpleReminder(long id, String name, String reminder, long dueDate) {
            this.id = id;
            this.name = name;
            this.reminder = reminder;
            this.dueDate = dueDate;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getReminder() {
            return reminder;
        }

        public long getDueDate() {
            return dueDate;
        }
    }

}
