package com.digitalhelper.config;

import com.digitalhelper.entity.Task;
import com.digitalhelper.entity.Vendor;
import com.digitalhelper.repository.TaskRepository;
import com.digitalhelper.repository.VendorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final VendorRepository vendorRepository;
    private final TaskRepository taskRepository;

    public DataInitializer(VendorRepository vendorRepository, TaskRepository taskRepository) {
        this.vendorRepository = vendorRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) {
        seedVendors();
        seedTasks();
    }

    private void seedVendors() {
        List<String[]> vendors = List.of(
                // 시중은행
                new String[]{"국민은행", "금융"}, new String[]{"신한은행", "금융"},
                new String[]{"우리은행", "금융"}, new String[]{"하나은행", "금융"},
                new String[]{"농협은행", "금융"}, new String[]{"기업은행", "금융"},
                new String[]{"SC제일은행", "금융"}, new String[]{"씨티은행", "금융"},
                new String[]{"수협은행", "금융"}, new String[]{"부산은행", "금융"},
                new String[]{"경남은행", "금융"}, new String[]{"광주은행", "금융"},
                new String[]{"전북은행", "금융"}, new String[]{"제주은행", "금융"},
                new String[]{"대구은행", "금융"}, new String[]{"새마을금고", "금융"},
                new String[]{"신협", "금융"}, new String[]{"우체국", "금융"},
                // 인터넷은행
                new String[]{"카카오뱅크", "금융"}, new String[]{"토스뱅크", "금융"},
                new String[]{"케이뱅크", "금융"},
                // 핀테크
                new String[]{"토스", "금융"}, new String[]{"카카오페이", "금융"},
                new String[]{"네이버페이", "금융"}, new String[]{"페이코", "금융"},
                // 증권
                new String[]{"미래에셋증권", "금융"}, new String[]{"NH투자증권", "금융"},
                new String[]{"삼성증권", "금융"}, new String[]{"키움증권", "금융"},
                new String[]{"한국투자증권", "금융"}, new String[]{"KB증권", "금융"}
        );

        for (String[] v : vendors) {
            if (!vendorRepository.existsByName(v[0])) {
                vendorRepository.save(new Vendor(v[0], v[1]));
            }
        }
    }

    private void seedTasks() {
        List<String[]> tasks = List.of(
                new String[]{"계좌이체",    "계좌이체 / 송금",  "[\"계좌이체\",\"송금\",\"이체\",\"보내\"]"},
                new String[]{"잔액조회",    "잔액 조회",        "[\"잔액\",\"조회\",\"얼마\"]"},
                new String[]{"공과금납부",  "공과금 납부",      "[\"공과금\",\"납부\",\"요금\"]"},
                new String[]{"OTP발급",    "OTP 발급",         "[\"otp\",\"일회용비밀번호\"]"},
                new String[]{"계좌개설",    "계좌 개설",        "[\"계좌개설\",\"계좌 만들기\",\"통장개설\",\"통장 만들기\"]"},
                new String[]{"이체한도변경", "이체 한도 변경",   "[\"이체한도\",\"송금한도\",\"한도 변경\",\"한도변경\"]"}
        );

        for (String[] t : tasks) {
            if (!taskRepository.existsByName(t[0])) {
                taskRepository.save(new Task(t[0], t[1], t[2]));
            }
        }
    }
}
