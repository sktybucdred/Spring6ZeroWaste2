package projekt.zespolowy.zero_waste.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projekt.zespolowy.zero_waste.dto.chat.ChatMessageDTO;
import projekt.zespolowy.zero_waste.entity.User;
import projekt.zespolowy.zero_waste.entity.EcoImpactHistory;
import projekt.zespolowy.zero_waste.repository.EcoImpactHistoryRep;
import projekt.zespolowy.zero_waste.repository.UserRepository;
import projekt.zespolowy.zero_waste.services.chat.ChatMessageService;

import java.time.LocalDate;
import java.util.List;

@Service
public class MonthlyReportService {

    @Autowired
    private EcoImpactHistoryRep ecoImpactHistoryRep;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private RankingService rankingService;  // Внедряем RankingService

    public void generateAndSendMonthlyReports(User admin) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now();

        List<User> users = userRepository.findAll();

        for (User user : users) {

            String reportContent = generateReportContent(user, startOfMonth, endOfMonth);

            ChatMessageDTO message = new ChatMessageDTO(
                    admin.getId().toString(),
                    user.getId().toString(),
                    reportContent,
                    LocalDate.now().toString()
            );

            chatMessageService.saveMessage(message, admin, user);
        }
    }

    private String generateReportContent(User user, LocalDate start, LocalDate end) {

        List<EcoImpactHistory> historyList = ecoImpactHistoryRep
                .findByUserIdAndDateBetween(user.getId(), start, end);


        double totalWater = historyList.stream().mapToDouble(EcoImpactHistory::getWaterSaved).sum();
        double totalCo2 = historyList.stream().mapToDouble(EcoImpactHistory::getCo2Saved).sum();
        double totalEnergy = historyList.stream().mapToDouble(EcoImpactHistory::getEnergySaved).sum();
        double totalWaste = historyList.stream().mapToDouble(EcoImpactHistory::getWasteReduced).sum();
       


        int rank = rankingService.getUserRank(user);


        String report = String.format(
                "Monthly Report for %s:\n" +
                        "- Water Saved: %.2f liters\n" +
                        "- CO₂ Reduced: %.2f kg\n" +
                        "- Energy Saved: %.2f kWh\n" +
                        "- Waste Reduced: %.2f kg\n" +
                        "- Current Rank: %d\n",
                user.getUsername(), totalWater, totalCo2, totalEnergy, totalWaste, rank
        );

        if (rank <= 10) {
            report += "Congratulations! You are in the top 10! Keep up the great work!";
        } else if (rank <= 50) {
            report += "Great job! You are in the top 50. Keep going and aim for the top!";
        } else {
            report += "You are doing well, but there is room for improvement. Keep striving for better results!";
        }

        return report;
    }
}
