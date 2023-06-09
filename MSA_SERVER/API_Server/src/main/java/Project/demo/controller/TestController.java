package Project.demo.controller;


import Project.demo.DTO.TestDTO;
import Project.demo.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;
    @RequestMapping(value = "/services1/",method = RequestMethod.GET)
    public Object test()
    {
        return "주소 뒤에 /user를 붙이면 모든 신고된 번호 및 신고 횟수 출력 /user/[휴대전화번호]입력 시 입력된 휴대전화번호의 신고값을 출력";
    }

    @RequestMapping(value = "/services1/user", method = RequestMethod.GET)
    public List<TestDTO> getUser()
    {
        return testService.getUserList();
    }


    @RequestMapping(value = "/services1/user/{phone}", method = RequestMethod.GET)
    public List<TestDTO> getNumber(@PathVariable String phone)
    {
        System.out.println(phone);
        return testService.SearchNumber(phone);
    }

    @RequestMapping(value = "/services1/report/{phone}", method = RequestMethod.GET)
    public List<TestDTO> InsertReport(@PathVariable String phone)
    {
        return testService.InsertReport(phone);
    }



}
