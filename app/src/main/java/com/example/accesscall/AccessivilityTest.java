package com.example.accesscall;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class AccessivilityTest extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 접근성 이벤트 처리 코드를 추가합니다.
    }

    @Override
    public void onInterrupt() {
        // 접근성 서비스가 중단되었을 때의 처리 코드를 추가합니다.
    }
}
