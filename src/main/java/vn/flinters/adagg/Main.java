package vn.flinters.adagg;

import com.google.inject.Guice;
import com.google.inject.Injector;
import picocli.CommandLine;
import vn.flinters.adagg.cli.AggregateCommand;

public final class Main {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AppModule());
        AggregateCommand cmd = injector.getInstance(AggregateCommand.class);

        int code = new CommandLine(cmd).execute(args);
        System.exit(code);
    }
}