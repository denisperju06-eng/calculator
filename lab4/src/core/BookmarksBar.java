package core;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * Bară orizontală pentru acces rapid la paginile Favorite (Bookmarks Bar).
 * Implementează FavoritesListener pentru actualizare automată conform șablonului Observer.
 */
public class BookmarksBar extends JPanel implements FavoritesListener {

    private static final long serialVersionUID = 1L;

    private final transient FavoritesManager favoritesManager;
    private final transient Consumer<String> onNavigateUrl;
    private final transient Consumer<String> onOpenInNewTab;
    private final transient Runnable onOpenManager;

    public BookmarksBar(FavoritesManager favoritesManager,
                        Consumer<String> onNavigateUrl,
                        Consumer<String> onOpenInNewTab,
                        Runnable onOpenManager) {
        super(new FlowLayout(FlowLayout.LEFT, 4, 2));
        this.favoritesManager = favoritesManager;
        this.onNavigateUrl = onNavigateUrl;
        this.onOpenInNewTab = onOpenInNewTab;
        this.onOpenManager = onOpenManager;

        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        setBackground(new Color(248, 249, 250));

        favoritesManager.addListener(this);
        refreshBar();
    }

    public void refreshBar() {
        removeAll();

        // Buton de administrare favorite
        JButton manageBtn = new JButton("⭐ Favorite");
        manageBtn.setFont(new Font("Dialog", Font.BOLD, 11));
        manageBtn.setToolTipText("Deschide managerul complet de Favorite");
        manageBtn.setFocusable(false);
        manageBtn.addActionListener(e -> {
            if (onOpenManager != null) onOpenManager.run();
        });
        add(manageBtn);

        List<FavoriteItem> list = favoritesManager.getFavorites();
        // Afișăm până la 12 favorite pe bară
        int count = Math.min(list.size(), 12);
        for (int i = 0; i < count; i++) {
            FavoriteItem item = list.get(i);
            JButton btn = createBookmarkButton(item);
            add(btn);
        }

        revalidate();
        repaint();
    }

    private JButton createBookmarkButton(FavoriteItem item) {
        String label = item.getTitle();
        if (label.length() > 18) {
            label = label.substring(0, 15) + "...";
        }

        JButton btn = new JButton("🔖 " + label);
        btn.setFont(new Font("Dialog", Font.PLAIN, 11));
        btn.setToolTipText(item.getTitle() + " (" + item.getUrl() + ")");
        btn.setFocusable(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        btn.setBackground(Color.WHITE);

        // Click stânga: navigare directă
        btn.addActionListener(e -> {
            if (onNavigateUrl != null) {
                onNavigateUrl.accept(item.getUrl());
            }
        });

        // Meniu contextual la click dreapta
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem openItem = new JMenuItem("Deschide");
        openItem.addActionListener(e -> {
            if (onNavigateUrl != null) onNavigateUrl.accept(item.getUrl());
        });

        JMenuItem openNewTabItem = new JMenuItem("Deschide în Tab Nou");
        openNewTabItem.addActionListener(e -> {
            if (onOpenInNewTab != null) onOpenInNewTab.accept(item.getUrl());
        });

        JMenuItem removeItem = new JMenuItem("Șterge din Favorite");
        removeItem.addActionListener(e -> {
            favoritesManager.removeFavoriteByUrl(item.getUrl());
        });

        contextMenu.add(openItem);
        contextMenu.add(openNewTabItem);
        contextMenu.addSeparator();
        contextMenu.add(removeItem);

        btn.setComponentPopupMenu(contextMenu);

        return btn;
    }

    @Override
    public void onFavoritesChanged() {
        refreshBar();
    }
}
