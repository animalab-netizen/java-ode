# java-ode

`java-ode` is the Java member of the ODE runtime family.

It keeps direct, chain, sequence and guard-oriented flows explicit for backend and JVM applications.

## Validation

```bash
javac -d out $(find src/main/java src/test/java -name "*.java")
java -ea -cp out io.github.animalab.ode.RuntimeTest
```
