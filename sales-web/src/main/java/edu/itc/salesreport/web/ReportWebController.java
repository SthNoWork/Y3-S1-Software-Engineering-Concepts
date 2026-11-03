package edu.itc.salesreport.web;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.ui.InputRules;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.Validation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.YearMonth;

@Controller
public class ReportWebController {
    private final Path dataDir = Path.of("data");
    private final Clock clock = Clock.systemDefaultZone();

    @GetMapping("/report")
    public String showReport(
            @RequestParam(name = "month", defaultValue = "2026-09") String monthStr,
            @RequestParam(name = "branch", defaultValue = "ALL") String branchStr,
            Model model
    ) {
        model.addAttribute("selectedMonth", monthStr);
        model.addAttribute("selectedBranch", branchStr);

        Validation<YearMonth> monthValidation = InputRules.month(monthStr, clock);
        if (monthValidation instanceof Validation.Invalid<YearMonth> inv) {
            model.addAttribute("error", inv.message());
            return "report";
        }

        Validation<String> branchValidation = InputRules.branch(branchStr);
        if (branchValidation instanceof Validation.Invalid<String> inv) {
            model.addAttribute("error", inv.message());
            return "report";
        }

        YearMonth month = ((Validation.Valid<YearMonth>) monthValidation).value();
        String branch = ((Validation.Valid<String>) branchValidation).value();

        SalesModel salesModel = new SalesModel();
        try {
            salesModel.load(dataDir, month);
            salesModel.selectBranch(branch);

            BranchSummary summary = salesModel.summary();
            model.addAttribute("summary", summary);
            model.addAttribute("transactions", salesModel.visibleTransactions());
        } catch (IOException e) {
            model.addAttribute("error", "No sales data found for month " + monthStr);
        }

        return "report";
    }
}

