# ApartmentFlow: Design System Setup

**Prepared By:** Senior Android UI Engineer
**Project:** ApartmentFlow
**Date:** August 2026

## Overview

A comprehensive Material Design 3 Design System has been established for **ApartmentFlow**, ensuring scalability, consistency, and a premium user experience.

## Components Implemented

The Design System is organized under `app/src/main/java/com/example/ui/` with the following structure:

```text
com.example.ui
├── theme
│   ├── Color.kt      // Defines Light/Dark palettes and semantic colors (Expense, Income, Success)
│   ├── Spacing.kt    // Unified spacing system (4.dp to 48.dp)
│   ├── Shape.kt      // Standardized Material 3 shapes (Small, Medium, Large)
│   ├── Type.kt       // Standardized typography scaling (Display, Headline, Title, Body, Label)
│   └── Theme.kt      // MaterialTheme configuration with Dynamic Color support (Android 12+)
└── components
    ├── Buttons.kt    // PrimaryButton, SecondaryButton, StandardTextButton, StandardIconButton, PrimaryFloatingActionButton
    ├── TextFields.kt // PrimaryTextField, PasswordTextField with visibility toggle
    ├── Cards.kt      // StandardCard, ElevatedStandardCard
    ├── Dialogs.kt    // StandardDialog for confirmations
    ├── Indicators.kt // FullScreenLoader
    ├── Chips.kt      // StatusChip
    ├── AppBars.kt    // StandardTopAppBar
    ├── Avatars.kt    // TextAvatar component
    └── Misc.kt       // CurrencyDisplay with color formatting
```

## Reusability Guidelines

-   **Colors:** Always use `MaterialTheme.colorScheme` instead of hardcoded colors to guarantee seamless Light/Dark mode transitions. For semantic colors, reference `ExpenseColor`, `IncomeColor`, `SuccessColor`, etc.
-   **Typography:** Use `MaterialTheme.typography` (e.g., `titleMedium`, `bodyLarge`) for all Text components to ensure responsive font scaling.
-   **Spacing:** Access standardized dimensions via `LocalSpacing.current` (e.g., `Modifier.padding(LocalSpacing.current.medium)`).
-   **Components:** Always utilize the wrappers defined in `ui/components` (e.g., `PrimaryButton`, `PrimaryTextField`, `StandardCard`). Never use base Compose elements directly for primary UI constructs. This enforces uniform styling and simplifies global design updates.
-   **Screen Sizes & Accessibility:** The use of standard Material 3 components inherently supports adaptive layouts and accessibility scaling. Always provide meaningful `contentDescription` for actionable icons and images.
