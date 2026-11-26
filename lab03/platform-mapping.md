# Cross-Platform Mapping: JavaFX MVVM to Android Jetpack & WPF

## 1. `ReportViewModel` Member Mapping

| `ReportViewModel` Member (JavaFX) | Android Jetpack (Kotlin) | WPF (.NET / C#) |
| :--- | :--- | :--- |
| `monthProperty()` (`ObjectProperty<YearMonth>`) | `MutableStateFlow<YearMonth?>` exposed as `StateFlow` + `onMonthChanged(YearMonth?)` | `YearMonth? Month` property raising `INotifyPropertyChanged` (`PropertyChanged`) |
| `branchSelectedProperty(b)` (`BooleanProperty`) | `MutableStateFlow<Map<String, Boolean>>` + `onBranchToggled(String, Boolean)` | `ObservableCollection<BranchOption>` where each item implements `INotifyPropertyChanged` |
| `summaries()` (`ObservableList<BranchSummary>`) | `StateFlow<List<BranchSummary>>` observed via `collectAsStateWithLifecycle()` | `ObservableCollection<BranchSummary>` bound to `DataGrid.ItemsSource` |
| `loadingProperty()` (`ReadOnlyBooleanProperty`) | `StateFlow<Boolean>` in `UiState` data class | `bool IsLoading` with `INotifyPropertyChanged` |
| `progressProperty()` (`ReadOnlyDoubleProperty`) | `StateFlow<Float>` (`0f..1f`) driving `LinearProgressIndicator` | `double Progress` (`0.0..1.0`) driving `ProgressBar.Value` |
| `validationMessageProperty()` (`StringBinding`) | Derived `StateFlow<String>` via `combine(month, branches) { ... }.stateIn(...)` | Computed getter `ValidationMessage` notified via `OnPropertyChanged` or `IDataErrorInfo` / `INotifyDataErrorInfo` |
| `generateCommand()` (`DelegateCommand`) | `fun generate()` called by `Button(enabled = uiState.canGenerate)` | `ICommand GenerateCommand` (`RelayCommand` / `AsyncRelayCommand` with `CanExecute`) |
| `cancelCommand()` (`DelegateCommand`) | `fun cancel()` cancelling the active `Job` (`job?.cancel()`) | `ICommand CancelCommand` triggering `CancellationTokenSource.Cancel()` |
| `BackgroundJobs` port (`FxBackgroundJobs`) | `viewModelScope.launch(Dispatchers.IO)` with `withContext(Dispatchers.Main)` | `async Task GenerateAsync(CancellationToken ct)` with `IProgress<double>` |

---

## 2. What Does Not Carry Over Directly

Jetpack Compose uses unidirectional data flow rather than JavaFX's mutable two-way property bindings, so `bindBidirectional` does not carry over and must be replaced with explicit state hoisting (`value` + `onValueChange` callbacks). Similarly, Android `ViewModel` instances survive configuration changes such as screen rotation, meaning `monthChoices` and loaded `summaries` stay alive while the Activity/Composable is recreated—a lifecycle concern that does not exist in desktop JavaFX or WPF windows. JavaFX's `FilteredList` and `Bindings.createStringBinding` auto-track observable dependencies at runtime, whereas Compose relies on explicit `combine`/`map` flows and WPF relies on manual `PropertyChanged` notifications or source-generated `[ObservableProperty]` attributes. Finally, JavaFX's `javafx.concurrent.Task` handles thread marshalling via `Platform.runLater` internally, whereas WPF uses `SynchronizationContext` via `await` and Android uses structured concurrency (`viewModelScope` + `Dispatchers.IO`).
