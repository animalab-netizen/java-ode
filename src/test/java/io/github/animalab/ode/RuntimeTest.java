package io.github.animalab.ode;

import java.util.List;

public final class RuntimeTest {
    public static void main(String[] args) {
        testDirect();
        testChain();
        testSequence();
        testGuard();
    }

    private static void testDirect() {
        var useCase = new Runtime.UseCase<String, String>(param -> "spotlight:" + param, null);
        var output = useCase.process("pikachu");
        assert output instanceof Runtime.ValueOutput<?>;
    }

    private static void testChain() {
        var first = new Runtime.UseCase<String, String>(param -> "bulbasaur", null);
        var chain = new Runtime.ChainUseCase<>(first, (result, param) -> result + " vs ivysaur");
        var output = chain.process("ignored");
        assert output instanceof Runtime.ValueOutput<?>;
    }

    private static void testSequence() {
        var sequence = new Runtime.SequenceUseCase<String, String>(value -> value);
        var output = sequence.process(List.of("bulbasaur", "charmander", "squirtle"));
        assert output instanceof Runtime.ValueOutput<?>;
    }

    private static void testGuard() {
        var useCase = new Runtime.UseCase<String[], String>(
            values -> values[0] + " vs " + values[1],
            values -> {
                if (values[0].equals(values[1])) {
                    throw new Runtime.GuardRejectedError("comparison requires distinct entries");
                }
            }
        );

        var output = useCase.process(new String[]{"pikachu", "pikachu"});
        assert output instanceof Runtime.ErrorOutput<?>;
    }
}
