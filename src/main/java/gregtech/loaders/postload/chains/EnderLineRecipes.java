package gregtech.loaders.postload.chains;

import static goodgenerator.api.recipe.GoodGeneratorRecipeMaps.neutronActivatorRecipes;
import static goodgenerator.util.MyRecipeAdder.computeRangeNKE;
import static gregtech.api.enums.Mods.AdvancedSolarPanel;
import static gregtech.api.enums.Mods.AppliedEnergistics2;
import static gregtech.api.enums.Mods.EtFuturumRequiem;
import static gregtech.api.enums.Mods.Forestry;
import static gregtech.api.enums.Mods.HardcoreEnderExpansion;
import static gregtech.api.enums.Mods.NewHorizonsCoreMod;
import static gregtech.api.enums.Mods.StevesCarts2;
import static gregtech.api.recipe.RecipeMaps.assemblerRecipes;
import static gregtech.api.recipe.RecipeMaps.autoclaveRecipes;
import static gregtech.api.recipe.RecipeMaps.blastFurnaceRecipes;
import static gregtech.api.recipe.RecipeMaps.centrifugeNonCellRecipes;
import static gregtech.api.recipe.RecipeMaps.chemicalPlantRecipes;
import static gregtech.api.recipe.RecipeMaps.compressorRecipes;
import static gregtech.api.recipe.RecipeMaps.distillationTowerRecipes;
import static gregtech.api.recipe.RecipeMaps.electrolyzerRecipes;
import static gregtech.api.recipe.RecipeMaps.fusionRecipes;
import static gregtech.api.recipe.RecipeMaps.mixerNonCellRecipes;
import static gregtech.api.recipe.RecipeMaps.multiblockChemicalReactorRecipes;
import static gregtech.api.recipe.RecipeMaps.sifterRecipes;
import static gregtech.api.recipe.RecipeMaps.vacuumFreezerRecipes;
import static gregtech.api.util.GTModHandler.getModItem;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static gregtech.api.util.GTRecipeBuilder.TICKS;
import static gregtech.api.util.GTRecipeConstants.CHEMPLANT_CASING_TIER;
import static gregtech.api.util.GTRecipeConstants.COIL_HEAT;
import static gregtech.api.util.GTRecipeConstants.FUSION_THRESHOLD;
import static gregtech.api.util.GTRecipeConstants.NKE_RANGE;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import goodgenerator.util.ItemRefer;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TierEU;
import gregtech.api.recipe.metadata.CompressionTierKey;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import gtPlusPlus.core.material.MaterialsAlloy;
import gtnhintergalactic.recipe.IGRecipeMaps;

public class EnderLineRecipes {

    static ItemStack missing = new ItemStack(Blocks.fire);

    public static void run() {

        // Ender Air Processing
        // if (EtFuturumRequiem.isModLoaded())
        {

            // GoG recipe only

            GTValues.RA.stdBuilder()
                .itemInputs(getModItem(EtFuturumRequiem.ID, "chorus_flower", 16, 0), GTUtility.getIntegratedCircuit(24))
                .itemOutputs(getModItem(EtFuturumRequiem.ID, "chorus_fruit_popped", 8, 0))
                .fluidInputs(
                    new FluidStack(FluidRegistry.getFluid("molten.uraniumtetrafluoride"), 1440),
                    new FluidStack(FluidRegistry.getFluid("vapor_of_levity"), 10),
                    new FluidStack(FluidRegistry.getFluid("liquid_sunshine"), 10))
                .fluidOutputs(Materials.EnderAir.getFluid(1_440))
                .duration(30 * SECONDS)
                .eut(TierEU.RECIPE_HV)
                .addTo(multiblockChemicalReactorRecipes);

            // Ender Air fusion

            GTValues.RA.stdBuilder()
                .fluidInputs(Materials.EnderAir.getFluid(144), new FluidStack(FluidRegistry.getFluid("ender"), 144))
                .fluidOutputs(Materials.EnderAirUnstable.getFluid(144))
                .duration(3 * SECONDS + 12 * TICKS)
                .eut(TierEU.RECIPE_ZPM)
                .metadata(FUSION_THRESHOLD, 24_000_000L)
                .addTo(fusionRecipes);

            // Ender Air Cryo

            GTValues.RA.stdBuilder()
                .itemInputs(GTOreDictUnificator.get(OrePrefixes.dust, Materials.CallistoIce, 8))
                .fluidInputs(
                    Materials.EnderAirUnstable.getFluid(10_000),
                    new FluidStack(FluidRegistry.getFluid("cryotheum"), 2_000))
                .itemOutputs(GTOreDictUnificator.get(OrePrefixes.dust, Materials.Snow, 8))
                .fluidOutputs(Materials.EnderAirCryostable.getFluid(10_000))
                .duration(15 * SECONDS)
                .eut(TierEU.RECIPE_UV)
                .addTo(vacuumFreezerRecipes);

            GTValues.RA.stdBuilder()
                .fluidInputs(Materials.EnderAirCryostable.getFluid(200_000))
                .fluidOutputs(
                    Materials.Radon.getGas(1_500),
                    new FluidStack(FluidRegistry.getFluid("xenon"), 2_000),
                    new FluidStack(FluidRegistry.getFluid("krypton"), 10_000),
                    Materials.Tritium.getGas(14_000),
                    Materials.TranslokiteUnstableVolatile.getFluid(16_040),
                    Materials.Helium.getGas(30_000),
                    Materials.Deuterium.getGas(100_000),
                    Materials.NitrogenDioxide.getGas(122_000))
                .itemOutputs(GTOreDictUnificator.get(OrePrefixes.dust, Materials.Cryotheum, 8))
                .duration(100 * SECONDS)
                .eut(TierEU.RECIPE_UV)
                .addTo(distillationTowerRecipes);

            // Ender Air Balanced

            GTValues.RA.stdBuilder()
                .fluidInputs(Materials.EnderAirUnstable.getFluid(10_000))
                .itemInputs(
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.EndSteel, 8),
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.Ichorium, 8),
                    GTUtility.getIntegratedCircuit(3))
                .itemOutputs(GTOreDictUnificator.get(OrePrefixes.ingotHot, Materials.Tungsten, 4))
                .fluidOutputs(Materials.EnderAirBalanced.getFluid(10_000))
                .duration(5 * SECONDS)
                .eut(TierEU.RECIPE_ZPM)
                .metadata(COIL_HEAT, 9001)
                .addTo(blastFurnaceRecipes);

            GTValues.RA.stdBuilder()
                .fluidInputs(Materials.EnderAirBalanced.getFluid(20_000))
                .itemOutputs(
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.Thaumium, 8L),
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.Silicon, 2L),
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.Magnesium, 1L),
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.Iron, 1L))
                .outputChances(7500, 5000, 2500, 1000)
                .fluidOutputs(Materials.TranslokiteUnstableUnbalanced.getFluid(15_400))
                .duration(20 * SECONDS)
                .eut(TierEU.RECIPE_ZPM)
                .addTo(electrolyzerRecipes);

            // Ender Air Fortified

            GTValues.RA.stdBuilder()
                .fluidInputs(
                    Materials.EnderAirUnstable.getFluid(10_000),
                    Materials.Galgadorian.getMolten(144),
                    Materials.ReinforcedGlass.getMolten(1_000))
                .fluidOutputs(Materials.EnderAirFortified.getFluid(10_000))
                .duration(10 * SECONDS)
                .eut(TierEU.RECIPE_LuV)
                .addTo(multiblockChemicalReactorRecipes);

            GTValues.RA.stdBuilder()
                .fluidInputs(Materials.EnderAirFortified.getFluid(20_000))
                .itemOutputs(
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.Endstone, 10L),
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.Glass, 5L),
                    getModItem(HardcoreEnderExpansion.ID, "end_powder", 4L, 0))
                .outputChances(5000, 2500, 2000)
                .fluidOutputs(Materials.TranslokiteUnstableSemifluid.getFluid(14_400))
                .duration(20 * SECONDS)
                .eut(TierEU.RECIPE_LuV)
                .addTo(sifterRecipes);

            // Ender Air Pyro

            GTValues.RA.stdBuilder()
                .fluidInputs(
                    Materials.EnderAirUnstable.getFluid(10_000),
                    new FluidStack(FluidRegistry.getFluid("pyrotheum"), 2_000))
                .itemInputs(
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.Firestone, 8),
                    MaterialsAlloy.ARCANITE.getDust(8))
                .fluidOutputs(Materials.EnderAirPyrostable.getFluid(10_000))
                .duration(2 * SECONDS)
                .eut(TierEU.RECIPE_IV)
                .addTo(mixerNonCellRecipes);

            GTValues.RA.stdBuilder()
                .fluidInputs(Materials.EnderAirPyrostable.getFluid(20_000))
                .itemOutputs(
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.AshDark, 10L),
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.Pyrotheum, 5L),
                    getModItem(HardcoreEnderExpansion.ID, "stardust", 5L, 0),
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.EnderPearl, 2L),
                    getModItem(HardcoreEnderExpansion.ID, "essence", 2L, 1),
                    getModItem(HardcoreEnderExpansion.ID, "essence", 1L, 0))
                .outputChances(5000, 4000, 2000, 1000, 500, 250)
                .fluidOutputs(Materials.TranslokiteUnstableHypercritical.getFluid(13_400))
                .duration(20 * SECONDS)
                .eut(TierEU.RECIPE_IV)
                .addTo(centrifugeNonCellRecipes);

        }

        // Translokite processing
        {
            // Neutron source alternate recipe

            GTValues.RA.stdBuilder()
                .itemInputs(ItemRefer.High_Density_Uranium.get(8))
                .fluidInputs(Materials.Steel.getMolten(41_472))
                .itemOutputs(ItemRefer.Neutron_Source.get(8))
                .duration(15 * SECONDS)
                .eut(TierEU.RECIPE_LuV)
                .addTo(autoclaveRecipes);

            // Neutron activator seed

            GTValues.RA.stdBuilder()
                .fluidInputs(Materials.TranslokiteUnstableAmalgam.getFluid(8_000))
                .itemInputs(
                    getModItem(EtFuturumRequiem.ID, "chorus_flower", 16, 0),
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.EnderEye, 4L),
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.Enderium, 1L),
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.EnderiumBase, 1L),
                    GTOreDictUnificator.get(OrePrefixes.round, Materials.Netherite, 1L),
                    ItemRefer.Neutron_Source.get(8))
                .itemOutputs(
                    ItemList.Resonant_Stable_Seed.get(8),
                    getModItem(AppliedEnergistics2.ID, "item.ItemMultiMaterial", 8, 6, missing),
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.EnderPearl, 4L),
                    GTOreDictUnificator.get(OrePrefixes.dust, Materials.Endium, 2L))
                .outputChances(10000, 9000, 5000, 2500)
                .fluidOutputs(Materials.NetherAir.getFluid(100))
                .duration(100 * SECONDS)
                .eut(0)
                .metadata(NKE_RANGE, computeRangeNKE(1100, 500))
                .addTo(neutronActivatorRecipes);
            // Bubble Encapsulation Polymer

            GTValues.RA.stdBuilder()
                .itemInputs(
                    GTOreDictUnificator.get(OrePrefixes.plate, Materials.VibrantAlloy, 2),
                    GTOreDictUnificator.get(OrePrefixes.sheet, Materials.EpoxidFiberReinforced, 2),
                    GTOreDictUnificator.get(OrePrefixes.plate, Materials.Soularium, 2),
                    GTModHandler.getModItem(NewHorizonsCoreMod.ID, "item.HeavyDutyPlateTier5", 1),
                    ItemList.Field_Generator_IV.get(1),
                    getModItem(Forestry.ID, "thermionicTubes", 8, 12),
                    ItemList.Field_Generator_MV.get(1),
                    ItemList.Emitter_LV.get(2),
                    getModItem(AdvancedSolarPanel.ID, "asp_crafting_items", 2, 1),
                    getModItem(StevesCarts2.ID, "ModuleComponents", 2, 20))
                .itemOutputs(ItemList.Bubble_Capsule_Polymer_Empty.get(16))
                .fluidInputs(
                    Materials.EnderAirPyrostable.getFluid(144L * 8),
                    Materials.EnderAirCryostable.getFluid(144L * 8),
                    Materials.EnderAirFortified.getFluid(144L * 8),
                    Materials.EnderAirBalanced.getFluid(144L * 8))
                .metadata(IGRecipeMaps.MODULE_TIER, 1)
                .duration(10 * SECONDS)
                .eut(TierEU.RECIPE_UV)
                .addTo(IGRecipeMaps.spaceAssemblerRecipes);

            // Unstable Amalgam

            GTValues.RA.stdBuilder()
                .itemInputs(GTOreDictUnificator.get(OrePrefixes.dust, Materials.Endium, 2L))
                .fluidInputs(
                    Materials.TranslokiteUnstableVolatile.getFluid(4_020),
                    Materials.TranslokiteUnstableUnbalanced.getFluid(4_020),
                    Materials.TranslokiteUnstableSemifluid.getFluid(4_020),
                    Materials.TranslokiteUnstableHypercritical.getFluid(4_020))
                .fluidOutputs(
                    Materials.TranslokiteUnstableAmalgam.getFluid(8_080),
                    FluidRegistry.getFluidStack("sludge", 6_000))
                .duration(33 * SECONDS)
                .eut(TierEU.RECIPE_UV)
                .metadata(CHEMPLANT_CASING_TIER, 7)
                .addTo(chemicalPlantRecipes);

            // Ender Goo recipe

            GTValues.RA.stdBuilder()
                .itemInputs(
                    ItemList.Resonant_Stable_Seed.get(1),
                    getModItem(HardcoreEnderExpansion.ID, "stardust", 1L, 0))
                .fluidInputs(
                    Materials.EnderAirPyrostable.getFluid(20_000),
                    Materials.EnderAirCryostable.getFluid(20_000),
                    Materials.EnderAirFortified.getFluid(20_000),
                    Materials.EnderAirBalanced.getFluid(20_000))
                .fluidOutputs(
                    Materials.EnderAirUnstable.getFluid(10_000),
                    FluidRegistry.getFluidStack("endergoo", 60_000))
                .duration(33 * SECONDS)
                .eut(TierEU.RECIPE_ZPM)
                .metadata(CHEMPLANT_CASING_TIER, 5)
                .addTo(chemicalPlantRecipes);

            // Formation Mixer vanish recipes

            GTValues.RA.stdBuilder()
                .fluidInputs(
                    FluidRegistry.getFluidStack("endergoo", 3_000),
                    Materials.TranslokiteUnstableSemifluid.getFluid(10_000),
                    Materials.TranslokiteUnstableUnbalanced.getFluid(10_000),
                    Materials.TranslokiteUnstableVolatile.getFluid(10_000))
                .fluidOutputs(Materials.TranslokiteUnstableAmalgam.getFluid(1_296))
                .duration(60 * SECONDS)
                .eut(TierEU.RECIPE_ZPM)
                .addTo(mixerNonCellRecipes);

            GTValues.RA.stdBuilder()
                .fluidInputs(
                    FluidRegistry.getFluidStack("endergoo", 3_000),
                    Materials.TranslokiteUnstableHypercritical.getFluid(10_000),
                    Materials.TranslokiteUnstableUnbalanced.getFluid(10_000),
                    Materials.TranslokiteUnstableVolatile.getFluid(10_000))
                .fluidOutputs(Materials.TranslokiteUnstableAmalgam.getFluid(1_296))
                .duration(60 * SECONDS)
                .eut(TierEU.RECIPE_ZPM)
                .addTo(mixerNonCellRecipes);

            GTValues.RA.stdBuilder()
                .fluidInputs(
                    FluidRegistry.getFluidStack("endergoo", 3_000),
                    Materials.TranslokiteUnstableHypercritical.getFluid(10_000),
                    Materials.TranslokiteUnstableSemifluid.getFluid(10_000),
                    Materials.TranslokiteUnstableVolatile.getFluid(10_000))
                .fluidOutputs(Materials.TranslokiteUnstableAmalgam.getFluid(1_296))
                .duration(60 * SECONDS)
                .eut(TierEU.RECIPE_ZPM)
                .addTo(mixerNonCellRecipes);

            GTValues.RA.stdBuilder()
                .fluidInputs(
                    FluidRegistry.getFluidStack("endergoo", 3_000),
                    Materials.TranslokiteUnstableHypercritical.getFluid(10_000),
                    Materials.TranslokiteUnstableSemifluid.getFluid(10_000),
                    Materials.TranslokiteUnstableUnbalanced.getFluid(10_000))
                .fluidOutputs(Materials.TranslokiteUnstableAmalgam.getFluid(1_296))
                .duration(60 * SECONDS)
                .eut(TierEU.RECIPE_ZPM)
                .addTo(mixerNonCellRecipes);

        }

        // Translokite processing
        {

            // FORMATION

            GTValues.RA.stdBuilder()
                .itemInputs(ItemList.Bubble_Capsule_Polymer_Empty.get(1))
                .fluidInputs(
                    FluidRegistry.getFluidStack("endergoo", 3_000),
                    Materials.TranslokiteUnstableSemifluid.getFluid(10_000),
                    Materials.TranslokiteUnstableUnbalanced.getFluid(10_000),
                    Materials.TranslokiteUnstableVolatile.getFluid(10_000))
                .fluidOutputs(
                    Materials.TranslokiteUnstableHypercritical.getFluid(1_000),
                    Materials.TranslokiteUnstableAmalgam.getFluid(1_296))
                .itemOutputs(ItemList.Bubble_Capsule_Polymer_Full.get(1))
                .duration(60 * SECONDS)
                .eut(TierEU.RECIPE_ZPM)
                .metadata(CHEMPLANT_CASING_TIER, 7)
                .addTo(chemicalPlantRecipes);

            GTValues.RA.stdBuilder()
                .itemInputs(ItemList.Bubble_Capsule_Polymer_Empty.get(1))
                .fluidInputs(
                    FluidRegistry.getFluidStack("endergoo", 3_000),
                    Materials.TranslokiteUnstableHypercritical.getFluid(10_000),
                    Materials.TranslokiteUnstableUnbalanced.getFluid(10_000),
                    Materials.TranslokiteUnstableVolatile.getFluid(10_000))
                .fluidOutputs(
                    Materials.TranslokiteUnstableSemifluid.getFluid(1_000),
                    Materials.TranslokiteUnstableAmalgam.getFluid(1_296))
                .itemOutputs(ItemList.Bubble_Capsule_Polymer_Full.get(1))
                .duration(60 * SECONDS)
                .eut(TierEU.RECIPE_ZPM)
                .metadata(CHEMPLANT_CASING_TIER, 7)
                .addTo(chemicalPlantRecipes);

            GTValues.RA.stdBuilder()
                .itemInputs(ItemList.Bubble_Capsule_Polymer_Empty.get(1))
                .fluidInputs(
                    FluidRegistry.getFluidStack("endergoo", 3_000),
                    Materials.TranslokiteUnstableHypercritical.getFluid(10_000),
                    Materials.TranslokiteUnstableSemifluid.getFluid(10_000),
                    Materials.TranslokiteUnstableVolatile.getFluid(10_000))
                .fluidOutputs(
                    Materials.TranslokiteUnstableUnbalanced.getFluid(1_000),
                    Materials.TranslokiteUnstableAmalgam.getFluid(1_296))
                .itemOutputs(ItemList.Bubble_Capsule_Polymer_Full.get(1))
                .duration(60 * SECONDS)
                .eut(TierEU.RECIPE_ZPM)
                .metadata(CHEMPLANT_CASING_TIER, 7)
                .addTo(chemicalPlantRecipes);

            GTValues.RA.stdBuilder()
                .itemInputs(ItemList.Bubble_Capsule_Polymer_Empty.get(1))
                .fluidInputs(
                    FluidRegistry.getFluidStack("endergoo", 3_000),
                    Materials.TranslokiteUnstableHypercritical.getFluid(10_000),
                    Materials.TranslokiteUnstableSemifluid.getFluid(10_000),
                    Materials.TranslokiteUnstableUnbalanced.getFluid(10_000))
                .fluidOutputs(
                    Materials.TranslokiteUnstableVolatile.getFluid(1_000),
                    Materials.TranslokiteUnstableAmalgam.getFluid(1_296))
                .itemOutputs(ItemList.Bubble_Capsule_Polymer_Full.get(1))
                .duration(60 * SECONDS)
                .eut(TierEU.RECIPE_ZPM)
                .metadata(CHEMPLANT_CASING_TIER, 7)
                .addTo(chemicalPlantRecipes);

            // STABILIZATION
            /*
             * GTValues.RA.stdBuilder()
             * .itemInputs(
             * getModItem(EtFuturumRequiem.ID, "chorus_fruit_popped", 8, 0),
             * ItemList.Resonant_Stable_Seed.get(1))
             * .fluidInputs(
             * Materials.Grade4PurifiedWater.getFluid(4_000),
             * Materials.TranslokiteSemistable.getFluid(10_000))
             * .fluidOutputs(Materials.TranslokiteStable.getFluid(7_500))
             * .itemOutputs(getModItem(HardcoreEnderExpansion.ID, "instability_orb", 1, 0))
             * .duration(20 * SECONDS)
             * .eut(TierEU.RECIPE_UV)
             * .metadata(CHEMPLANT_CASING_TIER, 7)
             * .addTo(chemicalPlantRecipes);
             */
        }

        EnderLineRecipes.addEncasedTranslokiteParts();
    }

    private static void addEncasedTranslokiteParts() {

        // Encased Translokite custom parts

        GTValues.RA.stdBuilder()
            .itemInputs(GTOreDictUnificator.get(OrePrefixes.plate, Materials.TranslokiteEncased, 64))
            .fluidInputs(Materials.EnderAirFortified.getFluid(144L * 64))
            .itemOutputs(GTOreDictUnificator.get(OrePrefixes.plateSuperdense, Materials.TranslokiteEncased, 1))
            .duration(500 * SECONDS)
            .eut(TierEU.RECIPE_UV)
            .metadata(CompressionTierKey.INSTANCE, 1)
            .addTo(compressorRecipes);

        GTValues.RA.stdBuilder()
            .itemInputs(
                GTOreDictUnificator.get(OrePrefixes.stick, Materials.TranslokiteEncased, 4),
                GTUtility.getIntegratedCircuit(4))
            .fluidInputs(Materials.EnderAirFortified.getFluid(144L))
            .itemOutputs(GTOreDictUnificator.get(OrePrefixes.frameGt, Materials.TranslokiteEncased, 1))
            .duration(3 * SECONDS + 4 * TICKS)
            .eut(TierEU.RECIPE_ZPM)
            .addTo(assemblerRecipes);

        // QFT Normal parts

    }

}
