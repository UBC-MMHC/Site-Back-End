package com.ubcmmhcsoftware;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithStructureTests {

    private final ApplicationModules modules = ApplicationModules.of(SiteApplication.class);

    @Test
    void modulesAreAcyclicAndRespectBoundaries() {
        modules.verify();
    }

    @Test
    void writeModuleCanvasForDocs() {
        new Documenter(modules).writeModulesAsPlantUml();
    }
}
