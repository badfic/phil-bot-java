package com.badfic.philbot.web.members;

import com.badfic.philbot.data.ReminderRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RemindersController extends BaseMembersController {

    private final ReminderRepository reminderRepository;

    public RemindersController(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    @GetMapping(value = "/reminders", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView get(HttpServletRequest httpServletRequest) throws Exception {
        checkSession(httpServletRequest, false);
        Guild guild = philJda.getGuildById(baseConfig.guildId);

        List<SimpleReminder> simpleReminderList = reminderRepository.findAll()
                .stream()
                .map(r -> {
                    long userId = r.getUserId();
                    Member memberById = guild.getMemberById(userId);

                    return new SimpleReminder(r.getId(), memberById != null ? memberById.getEffectiveName() : Long.toString(userId), r.getReminder(),
                            r.getDueDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                }).collect(Collectors.toList());

        Map<String, Object> props = new HashMap<>();
        props.put("pageTitle", "Reminders");
        addCommonProps(httpServletRequest.getSession(), props);
        props.put("reminders", simpleReminderList);

        return new ModelAndView("reminders", props);
    }

    private record SimpleReminder(long id, String name, String reminder, long dueDate) {}

}
