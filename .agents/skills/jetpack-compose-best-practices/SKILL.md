---
name: jetpack-compose-best-practices
description: Best practices, architectural guidelines, performance optimizations, recomposition tuning, and Material 3 design patterns for Jetpack Compose.
---

# Jetpack Compose Best Practices & Guidelines

This skill provides architectural standards, performance optimization techniques, and implementation patterns for modern Jetpack Compose applications.

---

## 1. State Management & Unidirectional Data Flow (UDF)

### State Hoisting
- Always hoist state to the lowest common ancestor of all composables that need it.
- **Pass state down, events up**: Composables should receive data as immutable values and notify ancestors of interactions via lambda callbacks.
- Avoid passing `ViewModel` instances to reusable UI components. Only screen-level root composables should interact directly with ViewModels.

```kotlin
// Bad: Tight coupling to ViewModel, un-previewable, untestable
@Composable
fun CycleCard(viewModel: DashboardViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Text(text = state.cycleDay)
}

// Good: Stateless, hoisted, easily previewable & testable
@Composable
fun CycleCard(
    cycleDay: String,
    onLogClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Text(text = cycleDay)
        Button(onClick = onLogClick) { Text("Log") }
    }
}
```

### State Preservation Across Process Death
- Use `rememberSaveable` for ephemeral UI state (e.g. text input field values, scroll positions, expanded sheet states) that should survive configuration changes and process recreation.
- For custom data classes with `rememberSaveable`, implement a custom `Saver` or use `@Parcelize`.

---

## 2. Recomposition Optimization & Stability

### Compose Compiler Stability Rules
Compose skips recomposition for a composable if all of its parameters are **stable** and unchanged.
- Standard Kotlin collections (`List<T>`, `Set<T>`, `Map<T>`) are treated as **unstable** by the Compose compiler unless using `kotlinx.collections.immutable` (e.g., `ImmutableList<T>`) or annotated wrapper classes.
- Annotate data models with `@Immutable` or `@Stable` when passed into composable parameters to guarantee skip optimization:

```kotlin
@Immutable
data class SymptomUiItem(
    val id: String,
    val name: String,
    val isSelected: Boolean
)
```

### Deferring State Reads
Read state at the lowest possible level in the layout tree. When state changes frequently (e.g., scroll position, animations), use lambda-based modifiers (`Modifier.offset { ... }`, `Modifier.drawWithContent { ... }`) to skip the composition and layout phases entirely:

```kotlin
// Bad: Triggers recomposition on every scroll pixel
Box(modifier = Modifier.offset(y = scrollState.value.dp))

// Good: Skips recomposition and layout phases, executes only in draw/placement
Box(modifier = Modifier.offset { IntOffset(x = 0, y = scrollState.value) })
```

### Using `derivedStateOf` Correctly
Use `derivedStateOf` only when a state input changes more frequently than you need the derived result to trigger recomposition:

```kotlin
val listState = rememberLazyListState()

// Good: derivedStateOf ensures recomposition ONLY when the boolean changes (e.g. false -> true)
val showScrollToTopButton by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 0 }
}
```

### Stable Keys in Lazy Lists
Always provide a unique, persistent key in `LazyColumn` and `LazyRow` to prevent unnecessary recompositions and preserve item scroll state/animations:

```kotlin
LazyColumn {
    items(
        items = cycleRecords,
        key = { record -> record.id }
    ) { record ->
        CycleHistoryItem(record = record)
    }
}
```

---

## 3. Side-Effects Lifecycle & Best Practices

| Side-Effect API | When to Use | Example |
| :--- | :--- | :--- |
| `LaunchedEffect(key)` | Launching coroutines tied to the composable lifecycle that should restart when `key` changes. | Triggering a one-time snackbar or starting an animation on state change. |
| `rememberCoroutineScope()` | Launching coroutines in response to direct user interactions. | Executing a suspend function inside an `onClick` callback. |
| `DisposableEffect(key)` | Requiring setup and clean-up (listeners, observers, sensor callbacks). | Registering a `LifecycleEventObserver` and removing it in `onDispose { ... }`. |
| `rememberUpdatedState()` | Capturing long-lived lambdas/values inside effects without re-triggering the effect. | Passing callback lambdas into long-running timers or flows. |
| `SideEffect` | Publishing Compose state changes to non-Compose code (e.g. analytics or system bars). | Updating system status bar style on theme change. |

---

## 4. Modifier Conventions & Layout Design

### Modifier Ordering & Rules
1. **Always accept a `modifier: Modifier = Modifier`** as the first optional parameter in reusable composables.
2. **Apply the passed `modifier` to the root layout element** of your composable.
3. Modifier order is sequential:
   ```kotlin
   // Padding applied BEFORE clickable increases the touch target
   Modifier
       .padding(8.dp)
       .clickable { /*...*/ }
       .padding(16.dp) // Internal padding inside clicked area
   ```
4. Use `Modifier.fillMaxWidth()` and explicit padding instead of hardcoded fixed widths where possible to support dynamic font scaling and multi-window split view.

### Handling Window Insets (Edge-to-Edge)
In edge-to-edge apps, consume insets gracefully:
```kotlin
Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing
) { innerPadding ->
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .imePadding() // Adjusts automatically when keyboard opens
    ) {
        // Screen Content
    }
}
```

---

## 5. Material 3 Theming & Typography

- Never hardcode hexadecimal color values (`Color(0xFF...)`) or absolute text sizes (`sp`) inside component bodies.
- Access system theme tokens via `MaterialTheme.colorScheme` and `MaterialTheme.typography`.
- Leverage `ProvideTextStyle` or surface tonal elevation for automatic contrast adjustments.

```kotlin
Text(
    text = "Cycle Insights",
    style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.onSurface
)
```

---

## 6. Multi-Preview Annotations & UI Testing

### Multi-Preview Setup
Define reusable preview annotations for Dark Mode, Font Scaling, and Screen Dimensions:

```kotlin
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
annotation class ThemePreviews

@ThemePreviews
@Composable
private fun CurrentCycleCardPreview() {
    WellnessTheme {
        CurrentCycleCard(
            cycleDay = 14,
            phaseName = "Follicular Phase",
            onLogClick = {}
        )
    }
}
```

### Compose Testing Quick Reference
```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun testCycleCardDisplaysCorrectDay() {
    composeTestRule.setContent {
        CurrentCycleCard(cycleDay = 14, phaseName = "Follicular Phase", onLogClick = {})
    }

    composeTestRule.onNodeWithText("Follicular Phase").assertIsDisplayed()
}
```
