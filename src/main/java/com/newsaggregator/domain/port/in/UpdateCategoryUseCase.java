package com.newsaggregator.domain.port.in;

import com.newsaggregator.domain.model.Category;

/**
 * Incoming Port für das Aktualisieren einer bestehenden Kategorie.
 */
public interface UpdateCategoryUseCase {

    /**
     * Aktualisiert eine bestehende Kategorie.
     *
     * @param id    Die ID der zu aktualisierenden Kategorie
     * @param name  Der neue Name der Kategorie
     * @param color Die neue Farbe der Kategorie
     * @param icon  Das neue Icon der Kategorie
     * @return Die aktualisierte Kategorie
     * @throws IllegalArgumentException wenn die Kategorie nicht gefunden wird
     * @throws IllegalArgumentException wenn Name ungültig ist
     */
    Category updateCategory(String id, String name, String color, String icon);
}
