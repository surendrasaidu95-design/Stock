
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new StockTradingApp().setVisible(true);
        });
    }
}

class StockTradingApp extends JFrame {

    // ---------- COLORS ----------
    static final Color BG = new Color(5, 18, 31);
    static final Color SIDEBAR = new Color(7, 25, 41);
    static final Color PANEL = new Color(10, 31, 49);
    static final Color PANEL_2 = new Color(14, 42, 63);
    static final Color BORDER = new Color(35, 76, 103);
    static final Color CYAN = new Color(0, 224, 210);
    static final Color BLUE = new Color(66, 153, 255);
    static final Color PURPLE = new Color(170, 95, 255);
    static final Color GREEN = new Color(0, 224, 160);
    static final Color RED = new Color(255, 82, 96);
    static final Color ORANGE = new Color(255, 174, 70);
    static final Color TEXT = new Color(238, 244, 250);
    static final Color MUTED = new Color(145, 172, 193);

    static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    // ---------- DATA ----------
    final User user = new User("Demo User", 100000.00);
    final Map<String, Stock> stocks = new LinkedHashMap<>();
    final Map<String, Holding> holdings = new LinkedHashMap<>();
    final java.util.List<Transaction> transactions = new ArrayList<>();
    final java.util.List<Double> portfolioHistory = new ArrayList<>();

    final CardLayout cardLayout = new CardLayout();
    final JPanel content = new JPanel(cardLayout);

    JLabel cashLabel;
    JLabel portfolioLabel;
    JLabel totalLabel;
    JLabel pnlLabel;
    JLabel holdingsCountLabel;
    JLabel transactionCountLabel;

    DefaultTableModel marketModel;
    DefaultTableModel portfolioModel;
    DefaultTableModel transactionModel;

    JTable marketTable;
    JTable portfolioTable;
    JTable transactionTable;

    javax.swing.Timer marketTimer;
    final Random random = new Random();

    StockTradingApp() {
        setTitle("Java Stock Trading Platform");
        setSize(1450, 900);
        setMinimumSize(new Dimension(1180, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initStocks();

        for (int i = 0; i < 20; i++) {
            portfolioHistory.add(0.0);
        }

        buildUI();
        refreshAll();
        startMarketTimer();
    }

    // ---------- STOCKS ----------
    void initStocks() {
        stocks.clear();
        addStock("TCS", 4202.00);
        addStock("INFY", 1813.00);
        addStock("RELIANCE", 2863.00);
        addStock("HDFCBANK", 1702.00);
        addStock("ICICIBANK", 1345.00);
        addStock("WIPRO", 600.00);
        addStock("SBIN", 850.00);
        addStock("ITC", 415.00);
        addStock("HCLTECH", 1580.00);
        addStock("MARUTI", 12450.00);
    }

    void addStock(String symbol, double price) {
        stocks.put(symbol, new Stock(symbol, price));
    }

    // ---------- UI ----------
    void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createBody(), BorderLayout.CENTER);

        setContentPane(root);
    }

    JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(18, 28, 18, 28));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = label("Java Stock Trading Platform", 29, TEXT, Font.BOLD);
        JLabel subtitle = label("Learn  •  Trade  •  Grow", 14, CYAN, Font.BOLD);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(5));
        titleBox.add(subtitle);

        JPanel userBox = new JPanel();
        userBox.setOpaque(false);
        userBox.setLayout(new BoxLayout(userBox, BoxLayout.Y_AXIS));

        JLabel userName = label("●  Demo User", 17, CYAN, Font.BOLD);
        JLabel role = label("Demo Trader", 12, MUTED, Font.PLAIN);

        userName.setAlignmentX(Component.RIGHT_ALIGNMENT);
        role.setAlignmentX(Component.RIGHT_ALIGNMENT);

        userBox.add(userName);
        userBox.add(Box.createVerticalStrut(4));
        userBox.add(role);

        header.add(titleBox, BorderLayout.WEST);
        header.add(userBox, BorderLayout.EAST);

        return header;
    }

    JPanel createBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BG);

        body.add(createSidebar(), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(BG);
        right.setBorder(new MatteBorder(1, 1, 0, 0, BORDER));

        content.setBackground(BG);

        content.add(createDashboard(), "dashboard");
        content.add(createMarketPage(), "market");
        content.add(createTradePage(), "trade");
        content.add(createPortfolioPage(), "portfolio");
        content.add(createTransactionsPage(), "transactions");

        right.add(content, BorderLayout.CENTER);
        body.add(right, BorderLayout.CENTER);

        return body;
    }

    JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(225, 0));
        sidebar.setBorder(new EmptyBorder(24, 14, 18, 14));

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

        String[] names = {
            "Dashboard",
            "Market",
            "Trade",
            "Portfolio",
            "Transactions"
        };

        for (String name : names) {
            JButton button = navButton(name);

            String pageName = name.toLowerCase();
            button.addActionListener(e -> cardLayout.show(content, pageName));

            nav.add(button);
            nav.add(Box.createVerticalStrut(10));
        }

        sidebar.add(nav, BorderLayout.NORTH);

        JPanel liveBox = new JPanel();
        liveBox.setOpaque(false);
        liveBox.setLayout(new BoxLayout(liveBox, BoxLayout.Y_AXIS));

        JLabel live = label("● Market Live", 13, CYAN, Font.BOLD);
        JLabel update = label("Prices update every 5 seconds", 11, MUTED, Font.PLAIN);

        liveBox.add(live);
        liveBox.add(Box.createVerticalStrut(5));
        liveBox.add(update);

        sidebar.add(liveBox, BorderLayout.SOUTH);

        return sidebar;
    }

    JButton navButton(String text) {
        JButton button = new JButton(text);

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(TEXT);
        button.setBackground(PANEL);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new CompoundBorder(
                new LineBorder(BORDER),
                new EmptyBorder(14, 18, 14, 12)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PANEL_2);
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(PANEL);
            }
        });

        return button;
    }

    // ---------- DASHBOARD ----------
    JPanel createDashboard() {
        JPanel page = pagePanel();

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titleBox = titleBlock(
                "Dashboard",
                "Your complete trading overview"
        );

        JButton reset = actionButton("RESET DEMO ACCOUNT", RED);
        reset.addActionListener(e -> resetAccount());

        top.add(titleBox, BorderLayout.WEST);
        top.add(reset, BorderLayout.EAST);

        page.add(top, BorderLayout.NORTH);

        JPanel main = new JPanel();
        main.setOpaque(false);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);

        cashLabel = valueLabel();
        portfolioLabel = valueLabel();
        totalLabel = valueLabel();
        pnlLabel = valueLabel();

        cards.add(statCard("AVAILABLE CASH", cashLabel, "Buying power", CYAN));
        cards.add(statCard("PORTFOLIO VALUE", portfolioLabel, "Current market value", BLUE));
        cards.add(statCard("TOTAL ACCOUNT VALUE", totalLabel, "Cash + investments", PURPLE));
        cards.add(statCard("PROFIT / LOSS", pnlLabel, "Overall performance", GREEN));

        main.add(cards);
        main.add(Box.createVerticalStrut(16));

        JPanel miniCards = new JPanel(new GridLayout(1, 2, 14, 0));
        miniCards.setOpaque(false);

        holdingsCountLabel = valueLabelSmall();
        transactionCountLabel = valueLabelSmall();

        miniCards.add(statCardSmall(
                "ACTIVE HOLDINGS",
                holdingsCountLabel,
                "Stocks currently owned",
                CYAN
        ));

        miniCards.add(statCardSmall(
                "TOTAL TRANSACTIONS",
                transactionCountLabel,
                "Completed orders",
                ORANGE
        ));

        main.add(miniCards);
        main.add(Box.createVerticalStrut(16));

        JPanel charts = new JPanel(new GridLayout(1, 2, 14, 0));
        charts.setOpaque(false);

        charts.add(section("PORTFOLIO PERFORMANCE", new PerformanceChart()));
        charts.add(section("PORTFOLIO DISTRIBUTION", new DistributionChart()));

        main.add(charts);
        main.add(Box.createVerticalStrut(16));

        JPanel bottom = new JPanel(new GridLayout(1, 2, 14, 0));
        bottom.setOpaque(false);

        bottom.add(section("MARKET OVERVIEW", createMarketTable()));
        bottom.add(section("QUICK ACTIONS", quickActions()));

        main.add(bottom);

        page.add(scroll(main), BorderLayout.CENTER);

        return page;
    }

    JLabel valueLabel() {
        return label("₹ 0.00", 23, TEXT, Font.BOLD);
    }

    JLabel valueLabelSmall() {
        return label("0", 22, TEXT, Font.BOLD);
    }

    JPanel statCard(String title, JLabel value, String footer, Color accent) {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setBorder(new CompoundBorder(
                new LineBorder(accent),
                new EmptyBorder(14, 18, 13, 18)
        ));

        panel.add(label(title, 12, MUTED, Font.BOLD));
        panel.add(Box.createVerticalStrut(8));
        panel.add(value);
        panel.add(Box.createVerticalStrut(6));
        panel.add(label(footer, 11, accent, Font.BOLD));

        return panel;
    }

    JPanel statCardSmall(String title, JLabel value, String footer, Color accent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        left.add(label(title, 11, MUTED, Font.BOLD));
        left.add(Box.createVerticalStrut(4));
        left.add(label(footer, 11, accent, Font.PLAIN));

        panel.add(left, BorderLayout.WEST);
        panel.add(value, BorderLayout.EAST);

        return panel;
    }

    JPanel quickActions() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton buy = actionButton("BUY STOCK", GREEN);
        JButton sell = actionButton("SELL STOCK", RED);
        JButton portfolio = actionButton("VIEW PORTFOLIO", BLUE);
        JButton transactionsButton = actionButton("TRANSACTION HISTORY", PURPLE);

        buy.addActionListener(e -> cardLayout.show(content, "trade"));
        sell.addActionListener(e -> cardLayout.show(content, "trade"));
        portfolio.addActionListener(e -> cardLayout.show(content, "portfolio"));
        transactionsButton.addActionListener(e -> cardLayout.show(content, "transactions"));

        panel.add(buy);
        panel.add(sell);
        panel.add(portfolio);
        panel.add(transactionsButton);

        return panel;
    }

    JButton actionButton(String text, Color accent) {
        JButton button = new JButton(text);

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(TEXT);
        button.setBackground(PANEL_2);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);

        button.setBorder(new CompoundBorder(
                new LineBorder(accent, 1),
                new EmptyBorder(13, 12, 13, 12)
        ));

        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(accent.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(PANEL_2);
            }
        });

        return button;
    }

    // ---------- MARKET ----------
    JPanel createMarketPage() {
        JPanel page = pagePanel();

        page.add(
                titleBlock("Market", "Live simulated market prices"),
                BorderLayout.NORTH
        );

        JPanel body = new JPanel(new BorderLayout(0, 15));
        body.setOpaque(false);

        body.add(
                section("MARKET WATCHLIST", createMarketTable()),
                BorderLayout.CENTER
        );

        body.add(
                label(
                        "Demo prices are simulated locally for this Java project.",
                        12,
                        MUTED,
                        Font.ITALIC
                ),
                BorderLayout.SOUTH
        );

        page.add(body, BorderLayout.CENTER);

        return page;
    }

    // ---------- TRADE ----------
    JPanel createTradePage() {
        JPanel page = pagePanel();

        page.add(
                titleBlock("Trade", "Buy and sell stocks using your demo balance"),
                BorderLayout.NORTH
        );

        JPanel tradePanels = new JPanel(new GridLayout(1, 2, 18, 0));
        tradePanels.setOpaque(false);

        tradePanels.add(createOrderPanel(true));
        tradePanels.add(createOrderPanel(false));

        page.add(scroll(tradePanels), BorderLayout.CENTER);

        return page;
    }

    JPanel createOrderPanel(boolean buy) {
        Color accent = buy ? GREEN : RED;

        JPanel panel = new JPanel();
        panel.setBackground(PANEL);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setBorder(new CompoundBorder(
                new LineBorder(accent, 1),
                new EmptyBorder(22, 25, 22, 25)
        ));

        panel.add(label(
                buy ? "BUY STOCK" : "SELL STOCK",
                24,
                TEXT,
                Font.BOLD
        ));

        panel.add(Box.createVerticalStrut(22));

        JComboBox<String> stockBox =
                new JComboBox<>(stocks.keySet().toArray(new String[0]));

        JSpinner quantity =
                new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));

        JLabel priceLabel = label("Current Price: ₹ 0.00", 17, accent, Font.BOLD);
        JLabel totalLabel = label("Order Total: ₹ 0.00", 15, TEXT, Font.BOLD);
        JLabel balanceLabel = label("", 12, MUTED, Font.PLAIN);
        JLabel messageLabel = label("", 12, MUTED, Font.BOLD);

        stockBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        quantity.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        styleInput(stockBox);
        styleInput(quantity);

        panel.add(label("STOCK SYMBOL", 12, MUTED, Font.BOLD));
        panel.add(Box.createVerticalStrut(6));
        panel.add(stockBox);

        panel.add(Box.createVerticalStrut(15));
        panel.add(label("QUANTITY", 12, MUTED, Font.BOLD));
        panel.add(Box.createVerticalStrut(6));
        panel.add(quantity);

        panel.add(Box.createVerticalStrut(18));
        panel.add(priceLabel);
        panel.add(Box.createVerticalStrut(7));
        panel.add(totalLabel);
        panel.add(Box.createVerticalStrut(7));
        panel.add(balanceLabel);

        panel.add(Box.createVerticalStrut(18));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);

        JButton execute = actionButton(
                buy ? "BUY STOCK" : "SELL STOCK",
                accent
        );

        JButton clear = actionButton("CLEAR", BLUE);

        buttons.add(execute);
        buttons.add(clear);

        panel.add(buttons);
        panel.add(Box.createVerticalStrut(15));
        panel.add(messageLabel);

        Runnable update = () -> {
            String symbol = String.valueOf(stockBox.getSelectedItem());
            Stock stock = stocks.get(symbol);

            if (stock == null) {
                return;
            }

            int qty = ((Number) quantity.getValue()).intValue();
            double total = stock.price * qty;

            priceLabel.setText("Current Price: ₹ " + money(stock.price));
            totalLabel.setText("Order Total: ₹ " + money(total));

            if (buy) {
                balanceLabel.setText(
                        "Available Cash: ₹ " + money(user.cash)
                );
            } else {
                Holding holding = holdings.get(symbol);
                int owned = holding == null ? 0 : holding.quantity;

                balanceLabel.setText(
                        "Available Shares: " + owned
                );
            }
        };

        stockBox.addActionListener(e -> update.run());
        quantity.addChangeListener(e -> update.run());

        execute.addActionListener(e -> {
            String symbol = String.valueOf(stockBox.getSelectedItem());
            int qty = ((Number) quantity.getValue()).intValue();

            boolean success;

            if (buy) {
                success = buyStock(symbol, qty);
            } else {
                success = sellStock(symbol, qty);
            }

            if (success) {
                messageLabel.setForeground(accent);
                messageLabel.setText(
                        buy
                                ? "✓ Buy order completed successfully."
                                : "✓ Sell order completed successfully."
                );
            } else {
                messageLabel.setForeground(RED);

                if (buy) {
                    messageLabel.setText(
                            "Order failed: insufficient cash or invalid quantity."
                    );
                } else {
                    messageLabel.setText(
                            "Order failed: insufficient shares or invalid quantity."
                    );
                }
            }

            refreshAll();
            update.run();
        });

        clear.addActionListener(e -> {
            stockBox.setSelectedIndex(0);
            quantity.setValue(1);
            messageLabel.setText("");
            update.run();
        });

        update.run();

        return panel;
    }

    // ---------- PORTFOLIO ----------
    JPanel createPortfolioPage() {
        JPanel page = pagePanel();

        page.add(
                titleBlock("Portfolio", "Your current stock holdings"),
                BorderLayout.NORTH
        );

        page.add(
                section("CURRENT HOLDINGS", createPortfolioTable()),
                BorderLayout.CENTER
        );

        return page;
    }

    // ---------- TRANSACTIONS ----------
    JPanel createTransactionsPage() {
        JPanel page = pagePanel();

        page.add(
                titleBlock("Transactions", "Complete buy and sell history"),
                BorderLayout.NORTH
        );

        page.add(
                section("TRANSACTION HISTORY", createTransactionTable()),
                BorderLayout.CENTER
        );

        return page;
    }

    // ---------- TABLES ----------
    JComponent createMarketTable() {
        if (marketModel == null) {
            marketModel = new DefaultTableModel(
                    new Object[]{"Symbol", "Price", "Change", "%", "Status"},
                    0
            ) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            marketTable = new JTable(marketModel);
            styleTable(marketTable);
        }

        return scroll(marketTable);
    }

    JComponent createPortfolioTable() {
        if (portfolioModel == null) {
            portfolioModel = new DefaultTableModel(
                    new Object[]{
                            "Symbol",
                            "Qty",
                            "Avg Price",
                            "Current Price",
                            "Market Value",
                            "P/L"
                    },
                    0
            ) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            portfolioTable = new JTable(portfolioModel);
            styleTable(portfolioTable);
        }

        return scroll(portfolioTable);
    }

    JComponent createTransactionTable() {
        if (transactionModel == null) {
            transactionModel = new DefaultTableModel(
                    new Object[]{
                            "Time",
                            "Type",
                            "Symbol",
                            "Qty",
                            "Price",
                            "Total"
                    },
                    0
            ) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            transactionTable = new JTable(transactionModel);
            styleTable(transactionTable);
        }

        return scroll(transactionTable);
    }

    void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setBackground(PANEL);
        table.setGridColor(BORDER);
        table.setRowHeight(34);

        table.setSelectionBackground(new Color(23, 76, 101));
        table.setSelectionForeground(Color.WHITE);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setBackground(PANEL_2);
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();

        renderer.setForeground(TEXT);
        renderer.setBackground(PANEL);

        table.setDefaultRenderer(Object.class, renderer);
    }

    // ---------- PANELS ----------
    JPanel titleBlock(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(label(title, 32, TEXT, Font.BOLD));
        panel.add(Box.createVerticalStrut(5));
        panel.add(label(subtitle, 14, MUTED, Font.PLAIN));

        return panel;
    }

    JPanel pagePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        return panel;
    }

    JPanel section(String title, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL);

        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER),
                new EmptyBorder(15, 16, 16, 16)
        ));

        panel.add(
                label(title, 16, TEXT, Font.BOLD),
                BorderLayout.NORTH
        );

        panel.add(component, BorderLayout.CENTER);

        return panel;
    }

    JScrollPane scroll(JComponent component) {
        JScrollPane scroll = new JScrollPane(component);

        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        return scroll;
    }

    void styleInput(JComponent component) {
        component.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42)
        );
    }

    JLabel label(
            String text,
            int size,
            Color color,
            int style
    ) {
        JLabel label = new JLabel(text);

        label.setFont(
                new Font("Segoe UI", style, size)
        );

        label.setForeground(color);

        return label;
    }

    String money(double value) {
        return MONEY.format(value);
    }

    // ---------- BUY ----------
    boolean buyStock(String symbol, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        Stock stock = stocks.get(symbol);

        if (stock == null) {
            return false;
        }

        double total = stock.price * quantity;

        if (user.cash < total) {
            return false;
        }

        user.cash -= total;

        Holding holding = holdings.get(symbol);

        if (holding == null) {
            holding = new Holding(symbol, 0, 0.0);
        }

        double oldCost =
                holding.quantity * holding.averagePrice;

        holding.quantity += quantity;

        holding.averagePrice =
                (oldCost + total) / holding.quantity;

        holdings.put(symbol, holding);

        transactions.add(
                new Transaction(
                        "BUY",
                        symbol,
                        quantity,
                        stock.price
                )
        );

        return true;
    }

    // ---------- SELL ----------
    boolean sellStock(String symbol, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        Stock stock = stocks.get(symbol);
        Holding holding = holdings.get(symbol);

        if (stock == null ||
                holding == null ||
                holding.quantity < quantity) {
            return false;
        }

        double total = stock.price * quantity;

        holding.quantity -= quantity;
        user.cash += total;

        transactions.add(
                new Transaction(
                        "SELL",
                        symbol,
                        quantity,
                        stock.price
                )
        );

        if (holding.quantity == 0) {
            holdings.remove(symbol);
        }

        return true;
    }

    // ---------- REFRESH ----------
    void refreshAll() {
        double portfolioValue = 0.0;

        for (Holding holding : holdings.values()) {
            Stock stock = stocks.get(holding.symbol);

            if (stock != null) {
                portfolioValue +=
                        holding.quantity * stock.price;
            }
        }

        double totalAccountValue =
                user.cash + portfolioValue;

        double pnl =
                totalAccountValue - 100000.00;

        if (cashLabel != null) {
            cashLabel.setText(
                    "₹ " + money(user.cash)
            );

            portfolioLabel.setText(
                    "₹ " + money(portfolioValue)
            );

            totalLabel.setText(
                    "₹ " + money(totalAccountValue)
            );

            if (pnl >= 0) {
                pnlLabel.setText(
                        "+ ₹ " + money(pnl)
                );
                pnlLabel.setForeground(GREEN);
            } else {
                pnlLabel.setText(
                        "- ₹ " + money(Math.abs(pnl))
                );
                pnlLabel.setForeground(RED);
            }

            holdingsCountLabel.setText(
                    String.valueOf(holdings.size())
            );

            transactionCountLabel.setText(
                    String.valueOf(transactions.size())
            );
        }

        updateMarketTable();
        updatePortfolioTable();
        updateTransactionTable();

        portfolioHistory.add(totalAccountValue);

        while (portfolioHistory.size() > 24) {
            portfolioHistory.remove(0);
        }

        content.repaint();
    }

    void updateMarketTable() {
        if (marketModel == null) {
            return;
        }

        marketModel.setRowCount(0);

        for (Stock stock : stocks.values()) {
            String status;

            if (stock.change > 0) {
                status = "UP";
            } else if (stock.change < 0) {
                status = "DOWN";
            } else {
                status = "FLAT";
            }

            marketModel.addRow(
                    new Object[]{
                            stock.symbol,
                            "₹ " + money(stock.price),
                            String.format("%+.2f", stock.change),
                            String.format(
                                    "%+.2f%%",
                                    stock.changePct
                            ),
                            status
                    }
            );
        }
    }

    void updatePortfolioTable() {
        if (portfolioModel == null) {
            return;
        }

        portfolioModel.setRowCount(0);

        for (Holding holding : holdings.values()) {
            Stock stock = stocks.get(holding.symbol);

            if (stock == null) {
                continue;
            }

            double marketValue =
                    holding.quantity * stock.price;

            double cost =
                    holding.quantity * holding.averagePrice;

            double profitLoss =
                    marketValue - cost;

            String pnlText =
                    (profitLoss >= 0 ? "+ ₹ " : "- ₹ ")
                            + money(Math.abs(profitLoss));

            portfolioModel.addRow(
                    new Object[]{
                            holding.symbol,
                            holding.quantity,
                            "₹ " + money(holding.averagePrice),
                            "₹ " + money(stock.price),
                            "₹ " + money(marketValue),
                            pnlText
                    }
            );
        }
    }

    void updateTransactionTable() {
        if (transactionModel == null) {
            return;
        }

        transactionModel.setRowCount(0);

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "dd-MM-yyyy HH:mm:ss"
                );

        for (int i = transactions.size() - 1;
             i >= 0;
             i--) {

            Transaction transaction =
                    transactions.get(i);

            transactionModel.addRow(
                    new Object[]{
                            format.format(transaction.time),
                            transaction.type,
                            transaction.symbol,
                            transaction.quantity,
                            "₹ " + money(transaction.price),
                            "₹ " + money(
                                    transaction.price *
                                            transaction.quantity
                            )
                    }
            );
        }
    }

    // ---------- MARKET SIMULATION ----------
    void startMarketTimer() {
        marketTimer = new javax.swing.Timer(
                5000,
                e -> {
                    simulatePrices();
                    refreshAll();
                }
        );

        marketTimer.start();
    }

    void simulatePrices() {
        for (Stock stock : stocks.values()) {
            double oldPrice = stock.price;

            double percentMove =
                    (random.nextDouble() - 0.48) * 0.012;

            stock.price =
                    Math.max(
                            1.0,
                            stock.price *
                                    (1.0 + percentMove)
                    );

            stock.change =
                    stock.price - oldPrice;

            stock.changePct =
                    (stock.change / oldPrice) * 100.0;
        }
    }

    // ---------- RESET ----------
    void resetAccount() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Reset cash, holdings and transaction history?",
                "Reset Demo Account",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        user.cash = 100000.00;

        holdings.clear();
        transactions.clear();

        portfolioHistory.clear();

        for (int i = 0; i < 20; i++) {
            portfolioHistory.add(0.0);
        }

        initStocks();

        refreshAll();

        JOptionPane.showMessageDialog(
                this,
                "Demo account has been reset successfully.",
                "Reset Complete",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ---------- PERFORMANCE CHART ----------
    class PerformanceChart extends JPanel {

        PerformanceChart() {
            setPreferredSize(
                    new Dimension(300, 260)
            );
            setBackground(PANEL);
        }

        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g =
                    (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth();
            int height = getHeight();

            int left = 42;
            int right = 18;
            int top = 20;
            int bottom = 35;

            g.setColor(BORDER);

            for (int i = 0; i < 4; i++) {
                int y =
                        top +
                                i *
                                        (height - top - bottom)
                                        / 3;

                g.drawLine(
                        left,
                        y,
                        width - right,
                        y
                );
            }

            if (portfolioHistory.size() < 2) {
                g.dispose();
                return;
            }

            double max = 100000.0;

            for (double value : portfolioHistory) {
                max = Math.max(max, value);
            }

            double min = 0.0;

            for (double value : portfolioHistory) {
                min = Math.min(min, value);
            }

            double range =
                    Math.max(1.0, max - min);

            int count =
                    portfolioHistory.size();

            for (int i = 1; i < count; i++) {
                double previous =
                        portfolioHistory.get(i - 1);

                double current =
                        portfolioHistory.get(i);

                int x1 =
                        left +
                                (i - 1) *
                                        (width - left - right)
                                        / (count - 1);

                int x2 =
                        left +
                                i *
                                        (width - left - right)
                                        / (count - 1);

                int y1 =
                        height -
                                bottom -
                                (int) (
                                        ((previous - min) /
                                                range)
                                                *
                                                (height -
                                                        top -
                                                        bottom)
                                );

                int y2 =
                        height -
                                bottom -
                                (int) (
                                        ((current - min) /
                                                range)
                                                *
                                                (height -
                                                        top -
                                                        bottom)
                                );

                g.setColor(CYAN);
                g.setStroke(
                        new BasicStroke(
                                2.5f,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND
                        )
                );

                g.drawLine(
                        x1,
                        y1,
                        x2,
                        y2
                );

                g.fillOval(
                        x2 - 4,
                        y2 - 4,
                        8,
                        8
                );
            }

            g.setColor(MUTED);
            g.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            11
                    )
            );

            g.drawString(
                    "Portfolio trend",
                    left,
                    height - 8
            );

            g.dispose();
        }
    }

    // ---------- DISTRIBUTION CHART ----------
    class DistributionChart extends JPanel {

        DistributionChart() {
            setPreferredSize(
                    new Dimension(300, 260)
            );
            setBackground(PANEL);
        }

        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g =
                    (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            double total = 0.0;

            for (Holding holding : holdings.values()) {
                Stock stock =
                        stocks.get(holding.symbol);

                if (stock != null) {
                    total +=
                            holding.quantity *
                                    stock.price;
                }
            }

            if (total <= 0.0) {
                g.setColor(MUTED);
                g.setFont(
                        new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                13
                        )
                );

                g.drawString(
                        "No holdings yet",
                        30,
                        55
                );

                g.dispose();
                return;
            }

            int radius =
                    Math.min(
                            88,
                            Math.min(
                                    getWidth(),
                                    getHeight()
                            ) / 3
                    );

            int centerX =
                    getWidth() / 2 - 35;

            int centerY =
                    getHeight() / 2;

            Color[] colors = {
                    CYAN,
                    BLUE,
                    PURPLE,
                    GREEN,
                    ORANGE,
                    RED
            };

            double startAngle = 0.0;
            int index = 0;

            for (Holding holding : holdings.values()) {
                Stock stock =
                        stocks.get(holding.symbol);

                if (stock == null) {
                    continue;
                }

                double value =
                        holding.quantity *
                                stock.price;

                double sweep =
                        value / total * 360.0;

                g.setColor(
                        colors[
                                index %
                                        colors.length
                        ]
                );

                g.fillArc(
                        centerX - radius,
                        centerY - radius,
                        radius * 2,
                        radius * 2,
                        (int) startAngle,
                        (int) Math.ceil(sweep)
                );

                startAngle += sweep;
                index++;
            }

            g.setColor(PANEL);

            g.fillOval(
                    centerX - radius / 2,
                    centerY - radius / 2,
                    radius,
                    radius
            );

            g.setColor(TEXT);
            g.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            12
                    )
            );

            g.drawString(
                    holdings.size() + " Stocks",
                    centerX - 29,
                    centerY + 4
            );

            g.dispose();
        }
    }
}

// ---------- USER ----------
class User {

    private final String name;
    double cash;

    User(String name, double cash) {
        this.name = name;
        this.cash = cash;
    }

    public String getName() {
        return name;
    }

    public double getCash() {
        return cash;
    }
}

// ---------- STOCK ----------
class Stock {

    final String symbol;
    double price;
    double change;
    double changePct;

    Stock(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
        this.change = 0.0;
        this.changePct = 0.0;
    }
}

// ---------- HOLDING ----------
class Holding {

    final String symbol;
    int quantity;
    double averagePrice;

    Holding(
            String symbol,
            int quantity,
            double averagePrice
    ) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }
}

// ---------- TRANSACTION ----------
class Transaction {

    final String type;
    final String symbol;
    final int quantity;
    final double price;
    final Date time;

    Transaction(
            String type,
            String symbol,
            int quantity,
            double price
    ) {
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.time = new Date();
    }
}
