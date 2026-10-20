# Technical Debt

1. **Parameter `season` handling**
    * **Reason:** Propagating the `season` object through the domain, persistence, and cache key requires a cascading refactor. Changing just one layer silently breaks others.
    * **Trigger:** Resolve before starting the backtest with real data.

2. **Database V3 and `is_sealed` rule**
    * **Reason:** Implementing the `competition_seasons` table and the `is_sealed` rule (derived from fresh payload with `count > 0`) was postponed to focus on the statistical engine.
    * **Trigger:** Resolve before starting the backtest with real data.

3. **Score invariant and Optional getter**
    * **Reason:** Enforcing `Score` non-nullability in its constructor breaks existing mappers, DTOs, and tests that document the old API contract (where nulls are possible).
    * **Trigger:** Resolve before starting the backtest with real data.

4. **Prior for newly promoted teams**
    * **Reason:** Currently, teams with no historical data default to a neutral strength (1.0). Promoted teams are typically weaker than the league average, so this prior overestimates them.
    * **Trigger:** Resolve before starting the backtest with real data.

5. **Standard error of the holdout**
    * **Reason:** The calculation of the RPS standard error for n=60 was deferred until the core simulation engine is built.
    * **Trigger:** Resolve before starting the backtest with real data.

6. **Opponent strength adjustment (Strength of Schedule)**
    * **Reason:** The model currently evaluates team strength relative to the league average but does not adjust for the quality of opponents faced. Early in the season, unbalanced schedules can skew the ratings.
    * **Trigger:** Monitor during backtesting and note the RPS variance. Implement an iterative adjustment (e.g., maximum likelihood estimation) if accuracy degrades heavily in the first 10 matchdays.