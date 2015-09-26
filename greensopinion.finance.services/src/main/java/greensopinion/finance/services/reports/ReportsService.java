package greensopinion.finance.services.reports;

import static com.google.common.base.Preconditions.checkNotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import greensopinion.finance.services.domain.Transaction;
import greensopinion.finance.services.domain.Transactions;
import greensopinion.finance.services.domain.TransactionsService;
import greensopinion.finance.services.web.model.IncomeVersusExpensesReport;
import greensopinion.finance.services.web.model.PeriodTransactions;
import greensopinion.finance.services.web.model.IncomeVersusExpensesReport.Month;

public class ReportsService {
	private final TransactionsService transactionsService;

	@Inject
	ReportsService(TransactionsService transactionsService) {
		this.transactionsService = checkNotNull(transactionsService);
	}

	public IncomeVersusExpensesReport incomeVersusExpenses() {
		IncomeVersusExpensesReport report = new IncomeVersusExpensesReport();

		Transactions transactions = transactionsService.retrieve();

		ListMultimap<Long, Transaction> transactionsByMonth = ArrayListMultimap.create();
		for (Transaction transaction : transactions.getTransactions()) {
			Long yearMonth = yearMonth(transaction.getDate());
			transactionsByMonth.put(yearMonth, transaction);
		}
		List<Long> sortedMonths = new ArrayList<>(transactionsByMonth.keySet());
		Collections.sort(sortedMonths);
		for (final Long yearMonth : sortedMonths) {
			String name = monthName(yearMonth);
			report.addMonth(new Month(yearMonth, name, transactionsByMonth.get(yearMonth)));
		}
		return report;
	}

	public PeriodTransactions transactionsForMonth(long yearMonth) {
		Transactions transactions = transactionsService.retrieve();
		List<Transaction> elements = new ArrayList<>();
		for (Transaction transaction : transactions.getTransactions()) {
			Long transactionYearMonth = yearMonth(transaction.getDate());
			if (transactionYearMonth.equals(yearMonth)) {
				elements.add(transaction);
			}
		}
		return new PeriodTransactions(monthName(yearMonth), elements);
	}

	private String monthName(final Long yearMonth) {
		try {
			return readableYearMonthFormat().format(yearMonthFormat().parse(yearMonth.toString()));
		} catch (ParseException e) {
			throw Throwables.propagate(e);
		}
	}

	private Long yearMonth(Date date) {
		return Long.parseLong(yearMonthFormat().format(date));
	}

	private DateFormat yearMonthFormat() {
		return new SimpleDateFormat("yyyyMM");
	}

	private DateFormat readableYearMonthFormat() {
		return new SimpleDateFormat("MMMMMMMMMMM yyyy");
	};

}