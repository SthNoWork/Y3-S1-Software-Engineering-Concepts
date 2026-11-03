package edu.itc.salesreport.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportWebController.class)
class ReportWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getReportWithValidMonthAndBranchReturns200() throws Exception {
        mockMvc.perform(get("/report").param("month", "2026-09").param("branch", "PNH"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Angkor Mart Monthly Sales Report")));
    }

    @Test
    void getReportWithInvalidMonthShowsErrorMessage() throws Exception {
        mockMvc.perform(get("/report").param("month", "2026/09").param("branch", "PNH"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("'2026/09' is not a month in the form yyyy-MM")));
    }

    @Test
    void getReportWithFutureMonthShowsFutureError() throws Exception {
        mockMvc.perform(get("/report").param("month", "2030-01").param("branch", "PNH"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("is in the future")));
    }

    @Test
    void getReportWithInvalidBranchShowsErrorMessage() throws Exception {
        mockMvc.perform(get("/report").param("month", "2026-09").param("branch", "INVALID"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Unrecognized branch: INVALID")));
    }
}

