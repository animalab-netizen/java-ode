package io.github.animalab.ode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Runtime {
    private Runtime() {
    }

    public sealed interface Output<R> permits ValueOutput, ErrorOutput, EmptyOutput {
    }

    public record ValueOutput<R>(R value) implements Output<R> {
    }

    public record ErrorOutput<R>(Exception error) implements Output<R> {
    }

    public record EmptyOutput<R>() implements Output<R> {
    }

    public static final class Outputs {
        private Outputs() {
        }

        public static <R> ValueOutput<R> value(R value) {
            return new ValueOutput<>(value);
        }

        public static <R> ErrorOutput<R> error(Exception error) {
            return new ErrorOutput<>(error);
        }

        public static <R> EmptyOutput<R> empty() {
            return new EmptyOutput<>();
        }
    }

    public static class GuardRejectedError extends RuntimeException {
        public GuardRejectedError(String message) {
            super(message);
        }
    }

    public static class ConnectionError extends RuntimeException {
        public ConnectionError(String message) {
            super(message);
        }
    }

    public static class HttpError extends RuntimeException {
        public HttpError(int statusCode, String message) {
            super("http " + statusCode + ": " + message);
        }
    }

    public static class UnexpectedResponseError extends RuntimeException {
        public UnexpectedResponseError(String message) {
            super(message);
        }
    }

    public static class UseCase<P, R> {
        private final Function<P, R> execute;
        private final Consumer<P> guard;

        public UseCase(Function<P, R> execute, Consumer<P> guard) {
            this.execute = execute;
            this.guard = guard;
        }

        public Output<R> process(P param) {
            try {
                if (guard != null) {
                    guard.accept(param);
                }
                return Outputs.value(execute.apply(param));
            } catch (Exception error) {
                return Outputs.error(error);
            }
        }
    }

    public static class ChainUseCase<P, I, R> {
        private final UseCase<P, I> first;
        private final BiFunction<I, P, R> second;

        public ChainUseCase(UseCase<P, I> first, BiFunction<I, P, R> second) {
            this.first = first;
            this.second = second;
        }

        public Output<R> process(P param) {
            Output<I> firstOutput = first.process(param);
            if (firstOutput instanceof ValueOutput<I> valueOutput) {
                try {
                    return Outputs.value(second.apply(valueOutput.value(), param));
                } catch (Exception error) {
                    return Outputs.error(error);
                }
            }
            if (firstOutput instanceof ErrorOutput<I> errorOutput) {
                return Outputs.error(errorOutput.error());
            }
            return Outputs.empty();
        }
    }

    public static class SequenceUseCase<P, R> {
        private final Function<P, R> step;

        public SequenceUseCase(Function<P, R> step) {
            this.step = step;
        }

        public Output<List<R>> process(List<P> values) {
            if (values.isEmpty()) {
                return Outputs.empty();
            }

            try {
                List<R> ordered = new ArrayList<>();
                for (P value : values) {
                    ordered.add(step.apply(value));
                }
                return Outputs.value(ordered);
            } catch (Exception error) {
                return Outputs.error(error);
            }
        }
    }

    public static class UseCaseDispatcher {
        public <P, R> Output<R> dispatch(P param, UseCase<P, R> useCase, Consumer<Output<R>> publish) {
            Output<R> output = useCase.process(param);
            if (publish != null) {
                publish.accept(output);
            }
            return output;
        }
    }
}
