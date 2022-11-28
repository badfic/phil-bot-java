package com.badfic.philbot.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Map<String, Object> props = new HashMap<>();

        if (status != null) {
            try {
                int statusCode = Integer.parseInt(status.toString());

                if (statusCode == HttpStatus.NOT_FOUND.value()) {
                    props.put("pageTitle", "Page Not Found — The Swamp");
                    return new ModelAndView("404", props);
                }
            } catch (Exception ignored) {}
        }

        props.put("pageTitle", "Error — The Swamp");
        return new ModelAndView("error", props);
    }

}
