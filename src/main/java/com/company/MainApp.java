
package com.company;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MainApp extends Application {

    private final DepartmentDAO departmentDAO =
            new DepartmentDAO();

    private final EmployeeDAO employeeDAO =
            new EmployeeDAO();

    private final SalaryStatisticsDAO statisticsDAO =
            new SalaryStatisticsDAO();

    private final JsonService jsonService = new JsonService();

    private TableView<DepartmentRow> departmentTable;
    private TableView<Employee> employeeTable;
    private Label statusLabel;
    private Label employeeTitle;

    public static class DepartmentRow {
        private final Department department;
        private final int employeeCount;

        public DepartmentRow(Department department, int count) {
            this.department = department;
            this.employeeCount = count;
        }

        public Department getDepartment() {
            return department;
        }

        public int getEmployeeCount() {
            return employeeCount;
        }
    }

    @Override
    public void start(Stage stage) {

        Label title = new Label(
                "Система управління підприємством"
        );
        title.setStyle(
                "-fx-font-size: 22px; -fx-font-weight: bold;"
        );

        departmentTable = new TableView<>();

        TableColumn<DepartmentRow, String> nameColumn =
                new TableColumn<>("Підрозділ");

        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDepartment().getName()
                )
        );

        TableColumn<DepartmentRow, Number> countColumn =
                new TableColumn<>("Працівників");

        countColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue().getEmployeeCount()
                )
        );

        nameColumn.setPrefWidth(190);
        countColumn.setPrefWidth(120);

        departmentTable.getColumns().add(nameColumn);
        departmentTable.getColumns().add(countColumn);

        employeeTable = new TableView<>();

        TableColumn<Employee, String> firstNameColumn =
                new TableColumn<>("Ім'я");

        firstNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getFirstName()
                )
        );

        TableColumn<Employee, String> lastNameColumn =
                new TableColumn<>("Прізвище");

        lastNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getLastName()
                )
        );

        TableColumn<Employee, String> positionColumn =
                new TableColumn<>("Посада");

        positionColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getPosition()
                )
        );

        TableColumn<Employee, String> salaryColumn =
                new TableColumn<>("Зарплата");

        salaryColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getSalary().toPlainString()
                )
        );

        employeeTable.getColumns().add(firstNameColumn);
        employeeTable.getColumns().add(lastNameColumn);
        employeeTable.getColumns().add(positionColumn);
        employeeTable.getColumns().add(salaryColumn);

        employeeTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        employeeTitle = new Label("Працівники підрозділу");
        employeeTitle.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold;"
        );

        statusLabel = new Label("Завантаження даних...");

        Button refreshButton = new Button("Оновити дані");
        refreshButton.setOnAction(e -> loadDepartments());

        Button transferButton = new Button(
                "Масове переведення працівників"
        );
        transferButton.setOnAction(e -> transferEmployees());

        Button statisticsButton = new Button(
                "Статистика зарплат"
        );

        statisticsButton.setOnAction(e -> showSalaryStatistics());

        Button exportButton = new Button("Експорт JSON");
        exportButton.setOnAction(e -> exportJson());

        Button importButton = new Button("Імпорт JSON");
        importButton.setOnAction(e -> importJson());

        Button sortButton = new Button(
                "Сортувати за кількістю"
        );

        sortButton.setOnAction(e -> {
            countColumn.setSortType(
                    TableColumn.SortType.DESCENDING
            );
            departmentTable.getSortOrder().clear();
            departmentTable.getSortOrder().add(countColumn);
            departmentTable.sort();
        });

        Button addDepartmentButton = new Button("Додати");
        Button editDepartmentButton = new Button("Перейменувати");
        Button deleteDepartmentButton = new Button("Видалити");

        addDepartmentButton.setOnAction(e -> addDepartment());
        editDepartmentButton.setOnAction(e -> editDepartment());
        deleteDepartmentButton.setOnAction(e -> deleteDepartment());

        HBox departmentButtons = new HBox(
                8,
                addDepartmentButton,
                editDepartmentButton,
                deleteDepartmentButton
        );

        departmentTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, selected) -> {
                    if (selected != null) {
                        loadEmployees(
                                selected.getDepartment()
                        );
                    }
                });

        VBox leftPanel = new VBox(
                10,
                new Label("Підрозділи"),
                departmentTable,
                sortButton,
                departmentButtons
        );
        leftPanel.setPrefWidth(330);
        VBox.setVgrow(departmentTable, Priority.ALWAYS);

        Button addButton = new Button("Додати працівника");
        Button editButton = new Button("Редагувати");
        Button deleteButton = new Button("Видалити");

        addButton.setOnAction(e -> addEmployee());
        editButton.setOnAction(e -> editEmployee());
        deleteButton.setOnAction(e -> deleteEmployee());

        HBox employeeButtons = new HBox(
                10, addButton, editButton, deleteButton
        );

        VBox rightPanel = new VBox(
                10,
                employeeTitle,
                employeeTable,
                employeeButtons
        );
        VBox.setVgrow(employeeTable, Priority.ALWAYS);

        HBox tables = new HBox(15, leftPanel, rightPanel);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        HBox toolbar = new HBox(
                10,
                refreshButton,
                transferButton,
                statisticsButton,
                exportButton,
                importButton
        );

        VBox root = new VBox(
                15,
                title,
                toolbar,
                tables,
                statusLabel
        );

        root.setPadding(new Insets(20));
        VBox.setVgrow(tables, Priority.ALWAYS);

        Scene scene = new Scene(root, 1050, 600);

        stage.setTitle(
                "Company Management — Варіант 4"
        );
        stage.setScene(scene);
        stage.show();

        loadDepartments();
    }

    private void loadDepartments() {
        try {
            List<Department> departments =
                    departmentDAO.getAllDepartments();

            List<Employee> employees =
                    employeeDAO.getAllEmployees();

            List<DepartmentRow> rows =
                    new java.util.ArrayList<>();

            for (Department department : departments) {
                int count = 0;

                for (Employee employee : employees) {
                    if (employee.getDepartmentId()
                            == department.getId()) {
                        count++;
                    }
                }

                rows.add(new DepartmentRow(
                        department, count
                ));
            }

            departmentTable.setItems(
                    FXCollections.observableArrayList(rows)
            );

            employeeTable.getItems().clear();
            employeeTitle.setText("Працівники підрозділу");

            statusLabel.setText(
                    "Завантажено підрозділів: "
                    + departments.size()
                    + " | Працівників: "
                    + employees.size()
            );

        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void loadEmployees(Department department) {
        try {
            List<Employee> employees =
                    employeeDAO.getEmployeesByDepartment(
                            department.getId()
                    );

            employeeTable.setItems(
                    FXCollections.observableArrayList(employees)
            );

            employeeTitle.setText(
                    "Працівники: " + department.getName()
            );

            statusLabel.setText(
                    "У підрозділі "
                    + department.getName()
                    + " працівників: "
                    + employees.size()
            );

        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText("Помилка роботи з базою даних");
        alert.setContentText(message);
        alert.showAndWait();

        statusLabel.setText("Помилка: " + message);
    }

    private void addEmployee() {
        DepartmentRow selected = departmentTable
                .getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Спочатку виберіть підрозділ.");
            return;
        }

        Employee employee = showEmployeeDialog(null,
                selected.getDepartment().getId());

        if (employee != null) {
            try {
                employeeDAO.addEmployee(employee);
                refreshAndSelect(employee.getDepartmentId());
            } catch (SQLException e) {
                showError(e.getMessage());
            }
        }
    }

    private void editEmployee() {
        Employee selected = employeeTable
                .getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Виберіть працівника для редагування.");
            return;
        }

        Employee updated = showEmployeeDialog(
                selected, selected.getDepartmentId());

        if (updated != null) {
            try {
                employeeDAO.updateEmployee(updated);
                refreshAndSelect(updated.getDepartmentId());
            } catch (SQLException e) {
                showError(e.getMessage());
            }
        }
    }

    private void deleteEmployee() {
        Employee selected = employeeTable
                .getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Виберіть працівника для видалення.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Підтвердження");
        confirm.setHeaderText("Видалити працівника?");
        confirm.setContentText(
                selected.getFirstName() + " " + selected.getLastName()
        );

        Optional<ButtonType> answer = confirm.showAndWait();

        if (answer.isPresent()
                && answer.get() == ButtonType.OK) {
            try {
                employeeDAO.deleteEmployee(selected.getId());
                refreshAndSelect(selected.getDepartmentId());
            } catch (SQLException e) {
                showError(e.getMessage());
            }
        }
    }

    private Employee showEmployeeDialog(
            Employee existing, int departmentId) {

        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle(existing == null
                ? "Додавання працівника"
                : "Редагування працівника");

        ButtonType saveType = new ButtonType(
                "Зберегти", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(
                saveType, ButtonType.CANCEL);

        TextField firstName = new TextField();
        TextField lastName = new TextField();
        TextField position = new TextField();
        TextField salary = new TextField();

        if (existing != null) {
            firstName.setText(existing.getFirstName());
            lastName.setText(existing.getLastName());
            position.setText(existing.getPosition());
            salary.setText(existing.getSalary().toPlainString());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Ім'я:"), 0, 0);
        grid.add(firstName, 1, 0);
        grid.add(new Label("Прізвище:"), 0, 1);
        grid.add(lastName, 1, 1);
        grid.add(new Label("Посада:"), 0, 2);
        grid.add(position, 1, 2);
        grid.add(new Label("Зарплата:"), 0, 3);
        grid.add(salary, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane()
                .lookupButton(saveType);

        Runnable validateForm = () -> {
            try {
                validateEmployeeInput(
                        firstName.getText(),
                        lastName.getText(),
                        position.getText(),
                        salary.getText()
                );
                saveButton.setDisable(false);
            } catch (IllegalArgumentException ex) {
                saveButton.setDisable(true);
            }
        };

        firstName.textProperty().addListener(
                (obs, oldValue, newValue) -> validateForm.run()
        );
        lastName.textProperty().addListener(
                (obs, oldValue, newValue) -> validateForm.run()
        );
        position.textProperty().addListener(
                (obs, oldValue, newValue) -> validateForm.run()
        );
        salary.textProperty().addListener(
                (obs, oldValue, newValue) -> validateForm.run()
        );

        validateForm.run();

        dialog.setResultConverter(button -> {
            if (button != saveType) {
                return null;
            }

            return new Employee(
                    existing == null ? 0 : existing.getId(),
                    firstName.getText().trim(),
                    lastName.getText().trim(),
                    position.getText().trim(),
                    new BigDecimal(salary.getText().trim()
                            .replace(',', '.')),
                    departmentId
            );
        });

        return dialog.showAndWait().orElse(null);
    }

    private void validateEmployeeInput(
            String firstName, String lastName,
            String position, String salary) {

        if (firstName.isBlank() || lastName.isBlank()
                || position.isBlank()) {
            throw new IllegalArgumentException(
                    "Усі текстові поля обов'язкові.");
        }

        if (firstName.trim().length() > 100
                || lastName.trim().length() > 100
                || position.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Максимальна довжина поля — 100 символів.");
        }

        try {
            BigDecimal amount = new BigDecimal(
                    salary.trim().replace(',', '.'));

            if (amount.signum() < 0
                    || amount.scale() > 2
                    || amount.precision() - amount.scale() > 10) {
                throw new IllegalArgumentException(
                        "Зарплата має бути від 0 до 9999999999.99 "
                        + "із максимум двома знаками після коми.");
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Введіть коректну числову зарплату.");
        }
    }

    private void refreshAndSelect(int departmentId) {
        loadDepartments();

        for (DepartmentRow row : departmentTable.getItems()) {
            if (row.getDepartment().getId() == departmentId) {
                departmentTable.getSelectionModel().select(row);
                break;
            }
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Увага");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String askDepartmentName(String title, String initialName) {
        TextInputDialog dialog = new TextInputDialog(initialName);
        dialog.setTitle(title);
        dialog.setHeaderText("Введіть назву підрозділу:");
        dialog.setContentText("Назва:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return null;
        }

        String name = result.get().trim();

        if (name.isBlank() || name.length() > 100) {
            showWarning("Назва повинна містити від 1 до 100 символів.");
            return null;
        }

        return name;
    }

    private void addDepartment() {
        String name = askDepartmentName("Новий підрозділ", "");

        if (name == null) {
            return;
        }

        try {
            departmentDAO.addDepartment(name);
            loadDepartments();
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void editDepartment() {
        DepartmentRow selected = departmentTable
                .getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Спочатку виберіть підрозділ.");
            return;
        }

        Department department = selected.getDepartment();

        String name = askDepartmentName(
                "Перейменування підрозділу",
                department.getName()
        );

        if (name == null) {
            return;
        }

        try {
            departmentDAO.updateDepartment(department.getId(), name);
            refreshAndSelect(department.getId());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void deleteDepartment() {
        DepartmentRow selected = departmentTable
                .getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Спочатку виберіть підрозділ.");
            return;
        }

        if (selected.getEmployeeCount() > 0) {
            showWarning(
                    "Неможливо видалити підрозділ, у якому є працівники."
            );
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Підтвердження");
        confirm.setHeaderText("Видалити підрозділ?");
        confirm.setContentText(
                selected.getDepartment().getName()
        );

        Optional<ButtonType> answer = confirm.showAndWait();

        if (answer.isPresent() && answer.get() == ButtonType.OK) {
            try {
                departmentDAO.deleteDepartment(
                        selected.getDepartment().getId()
                );
                loadDepartments();
            } catch (SQLException e) {
                showError(e.getMessage());
            }
        }
    }

    private void transferEmployees() {
        List<Department> departments;

        try {
            departments = departmentDAO.getAllDepartments();
        } catch (SQLException e) {
            showError(e.getMessage());
            return;
        }

        if (departments.size() < 2) {
            showWarning(
                    "Для переведення потрібно щонайменше два підрозділи."
            );
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Масове переведення");
        dialog.setHeaderText(
                "Переведення всіх працівників між підрозділами"
        );

        ComboBox<Department> sourceBox = new ComboBox<>();
        ComboBox<Department> targetBox = new ComboBox<>();

        sourceBox.getItems().addAll(departments);
        targetBox.getItems().addAll(departments);

        sourceBox.setPromptText("Звідки");
        targetBox.setPromptText("Куди");

        DepartmentRow selected = departmentTable
                .getSelectionModel().getSelectedItem();

        if (selected != null) {
            for (Department department : departments) {
                if (department.getId()
                        == selected.getDepartment().getId()) {
                    sourceBox.setValue(department);
                    break;
                }
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        grid.add(new Label("З підрозділу:"), 0, 0);
        grid.add(sourceBox, 1, 0);
        grid.add(new Label("До підрозділу:"), 0, 1);
        grid.add(targetBox, 1, 1);

        ButtonType transferType = new ButtonType(
                "Перевести", ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                transferType, ButtonType.CANCEL
        );
        dialog.getDialogPane().setContent(grid);

        Button transfer = (Button) dialog.getDialogPane()
                .lookupButton(transferType);

        Runnable validate = () -> {
            Department source = sourceBox.getValue();
            Department target = targetBox.getValue();

            transfer.setDisable(
                    source == null
                    || target == null
                    || source.getId() == target.getId()
            );
        };

        sourceBox.valueProperty().addListener(
                (obs, oldValue, newValue) -> validate.run()
        );
        targetBox.valueProperty().addListener(
                (obs, oldValue, newValue) -> validate.run()
        );

        validate.run();

        dialog.setResultConverter(button -> button);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isEmpty() || result.get() != transferType) {
            return;
        }

        Department source = sourceBox.getValue();
        Department target = targetBox.getValue();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Підтвердження переведення");
        confirm.setHeaderText(
                "Перевести всіх працівників?"
        );
        confirm.setContentText(
                "З: " + source.getName()
                + "\nДо: " + target.getName()
        );

        Optional<ButtonType> answer = confirm.showAndWait();

        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            int count = employeeDAO.transferEmployees(
                    source.getId(), target.getId()
            );

            refreshAndSelect(target.getId());

            Alert success = new Alert(
                    Alert.AlertType.INFORMATION
            );
            success.setTitle("Переведення завершено");
            success.setHeaderText(null);
            success.setContentText(
                    "Успішно переведено працівників: " + count
            );
            success.showAndWait();

        } catch (SQLException | IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void showSalaryStatistics() {
        try {
            BigDecimal totalFund =
                    statisticsDAO.getTotalSalaryFund();

            List<SalaryStatistics> statistics =
                    statisticsDAO.getDepartmentStatistics();

            Stage statisticsStage = new Stage();
            statisticsStage.setTitle(
                    "Статистика заробітної плати"
            );

            Label title = new Label(
                    "Фонд заробітної плати підприємства"
            );
            title.setStyle(
                    "-fx-font-size: 20px; -fx-font-weight: bold;"
            );

            Label totalLabel = new Label(
                    "Загальний фонд: "
                    + formatMoney(totalFund) + " грн"
            );
            totalLabel.setStyle(
                    "-fx-font-size: 17px; -fx-font-weight: bold;"
            );

            TableView<SalaryStatistics> table =
                    new TableView<>();

            TableColumn<SalaryStatistics, String> nameCol =
                    new TableColumn<>("Підрозділ");

            nameCol.setCellValueFactory(data ->
                    new SimpleStringProperty(
                            data.getValue().getDepartmentName()
                    )
            );

            TableColumn<SalaryStatistics, Number> countCol =
                    new TableColumn<>("Працівників");

            countCol.setCellValueFactory(data ->
                    new SimpleIntegerProperty(
                            data.getValue().getEmployeeCount()
                    )
            );

            TableColumn<SalaryStatistics, String> fundCol =
                    new TableColumn<>("Фонд зарплати, грн");

            fundCol.setCellValueFactory(data ->
                    new SimpleStringProperty(
                            formatMoney(
                                    data.getValue().getSalaryFund()
                            )
                    )
            );

            TableColumn<SalaryStatistics, String> percentCol =
                    new TableColumn<>("Частка, %");

            percentCol.setCellValueFactory(data -> {
                BigDecimal percentage = BigDecimal.ZERO;

                if (totalFund.signum() > 0) {
                    percentage = data.getValue()
                            .getSalaryFund()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(
                                    totalFund,
                                    2,
                                    RoundingMode.HALF_UP
                            );
                }

                return new SimpleStringProperty(
                        percentage.toPlainString() + "%"
                );
            });

            table.getColumns().add(nameCol);
            table.getColumns().add(countCol);
            table.getColumns().add(fundCol);
            table.getColumns().add(percentCol);

            table.setColumnResizePolicy(
                    TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
            );

            table.setItems(
                    FXCollections.observableArrayList(statistics)
            );

            Button closeButton = new Button("Закрити");
            closeButton.setOnAction(e -> statisticsStage.close());

            VBox root = new VBox(
                    15,
                    title,
                    totalLabel,
                    table,
                    closeButton
            );

            root.setPadding(new Insets(20));
            VBox.setVgrow(table, Priority.ALWAYS);

            Scene scene = new Scene(root, 850, 500);
            statisticsStage.setScene(scene);
            statisticsStage.show();

        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private String formatMoney(BigDecimal value) {
        return String.format(
                Locale.forLanguageTag("uk-UA"),
                "%,.2f",
                value
        );
    }

    private void exportJson() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Експорт даних у JSON");
        fileChooser.setInitialFileName("company_backup.json");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "JSON файли (*.json)", "*.json"
                )
        );

        File file = fileChooser.showSaveDialog(
                departmentTable.getScene().getWindow()
        );

        if (file == null) {
            return;
        }

        try {
            jsonService.exportToJson(file);

            Alert success = new Alert(
                    Alert.AlertType.INFORMATION
            );
            success.setTitle("Експорт завершено");
            success.setHeaderText(null);
            success.setContentText(
                    "Дані успішно збережено:\n"
                    + file.getAbsolutePath()
            );
            success.showAndWait();

        } catch (SQLException | IOException e) {
            showError(
                    "Не вдалося експортувати JSON:\n"
                    + e.getMessage()
            );
        }
    }

    private void importJson() {
        FileChooser chooser = new FileChooser();

        chooser.setTitle("Імпорт даних із JSON");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "JSON файли (*.json)", "*.json"
                )
        );

        File file = chooser.showOpenDialog(
                departmentTable.getScene().getWindow()
        );

        if (file == null) {
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirm.setTitle("Підтвердження імпорту");
        confirm.setHeaderText(
                "Замінити поточні дані бази?"
        );
        confirm.setContentText(
                "Усі поточні підрозділи та працівники "
                + "будуть замінені даними з файлу:\n"
                + file.getName()
                + "\n\nПеред продовженням переконайтеся, "
                + "що резервну копію збережено."
        );

        Optional<ButtonType> answer = confirm.showAndWait();

        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            jsonService.importFromJson(file);

            loadDepartments();

            Alert success = new Alert(
                    Alert.AlertType.INFORMATION
            );
            success.setTitle("Імпорт завершено");
            success.setHeaderText(null);
            success.setContentText(
                    "Дані успішно відновлено з файлу:\n"
                    + file.getAbsolutePath()
            );
            success.showAndWait();

        } catch (SQLException | IOException e) {
            showError(
                    "Не вдалося імпортувати JSON:\n"
                    + e.getMessage()
            );
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
