package it.unibo.mvc;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

    private final static String FILE_PATH = System.getProperty("user.home") 
                + File.separator + "Desktop" + File.separator + "lab10" + File.separator 
                + "102-advanced-mvc" + File.separator + "src" + File.separator
                + "main" + File.separator + "resources" + File.separator + "config.yml";
    private final static String FILE_LOGGER_PATH = System.getProperty("user.home") 
                + File.separator + "Desktop" + File.separator + "lab10" + File.separator 
                + "102-advanced-mvc" + File.separator + "src" + File.separator
                + "main" + File.separator + "resources" + File.separator + "filelog.txt";
    private final static int NUM_LINES = 3;

    private final DrawNumber model;
    private final List<DrawNumberView> views;
    private int min;
    private int max;
    private int attempts;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        try (final BufferedReader fileRead = new BufferedReader(new FileReader(FILE_PATH))) {
            for (int i=0; i<NUM_LINES ; i++) {
                final String line = fileRead.readLine();
                final StringTokenizer stringTokenized = new StringTokenizer(line, ": ");
                final String stringToken = stringTokenized.nextToken();
                if (stringToken.trim().equals("minimum")) {
                    this.min = Integer.parseInt(stringTokenized.nextToken());
                }
                if (stringToken.trim().equals("maximum")) {
                   this.max = Integer.parseInt(stringTokenized.nextToken());
                }
                if (stringToken.trim().equals("attempts")) {
                    this.attempts = Integer.parseInt(stringTokenized.nextToken());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.model = new DrawNumberImpl(this.min, this.max, this.attempts);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(),
                        new DrawNumberViewImpl(),
                        new PrintStreamView(System.out),
                        new PrintStreamView(FILE_LOGGER_PATH));
    }

}
