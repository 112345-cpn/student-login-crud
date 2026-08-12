package org.example.studentlogincrud;

import org.junit.jupiter.api.Test;
import org.example.studentlogincrud.service.impl.StudentServiceImpl;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentLoginCrudApplicationTests {
    @Test
    void studentNumberMustBeTenDigitsStartingWith2600() throws Exception {
        Method method = StudentServiceImpl.class.getDeclaredMethod("isValidStudentNo", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(new StudentServiceImpl(), "2600123456"));
        assertFalse((Boolean) method.invoke(new StudentServiceImpl(), "2612345678"));
        assertFalse((Boolean) method.invoke(new StudentServiceImpl(), "260012345"));
        assertFalse((Boolean) method.invoke(new StudentServiceImpl(), "26001234567"));
    }
}
