package Project.demo.mapper;

import Project.demo.DTO.TestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
@Mapper

public interface TestMapper {
    List<TestDTO> getUserList();
    List<TestDTO> SearchNumber(String phone);
    List<TestDTO> InsertReport(String phone);

}


