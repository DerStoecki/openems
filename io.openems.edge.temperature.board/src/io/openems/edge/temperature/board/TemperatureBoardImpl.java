package io.openems.edge.temperature.board;

import io.openems.edge.bridge.spi.api.BridgeSpi;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.temperature.board.api.Adc;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Designate(ocd = Config.class, factory = true)
@Component(name = "CircuitBoard", immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE)

public class TemperatureBoardImpl extends AbstractOpenemsComponent implements ConsolinnoBoards, OpenemsComponent, TemperatureBoard {

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    BridgeSpi bridgeSpi;

    private String circuitBoardId;
    private String versionId;
    private short maxCapacity;
    private Set<Adc> adcList = new HashSet<>();

    public TemperatureBoardImpl() {
        super(OpenemsComponent.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        this.circuitBoardId = config.id();
        this.versionId = config.versionNumber();
        String adcFrequency = config.adcFrequency();
        String dipSwitches = config.dipSwitches();
        List<String> frequency = new ArrayList<>();
        List<Integer> dipSwitch = new ArrayList<>();

        if (adcFrequency.contains(";")) {
            String[] parts = adcFrequency.split(";");
            frequency.addAll(Arrays.asList(parts));
        } else {
            frequency.add(adcFrequency);
        }

        for (Character dipSwitchUse : dipSwitches.toCharArray()) {
            dipSwitch.add(Character.getNumericValue(dipSwitchUse));
        }
        createTemperatureBoard(this.versionId, frequency, dipSwitch);
    }

    private void createTemperatureBoard(String versionNumber, List<String> frequency, List<Integer> dipSwitch) {
        switch (versionNumber) {
            //more to come with further impl
            case "1":
                this.maxCapacity = TemperatureBoardVersions.TEMPERATURE_BOARD_V_1.getMaxSize();
                short counter = 0;
                for (Adc mcpWantToCreate : TemperatureBoardVersions.TEMPERATURE_BOARD_V_1.getMcpContainer()) {
                    createAdc(mcpWantToCreate, frequency.get(counter), dipSwitch.get(counter));
                    counter++;
                }
                break;
        }
    }

    private void createAdc(Adc mcpWantToCreate, String frequency, int dipSwitch) {
        mcpWantToCreate.initialize(dipSwitch, Integer.parseInt(frequency), this.circuitBoardId, this.versionId);
        this.adcList.add(mcpWantToCreate);
        bridgeSpi.addAdc(mcpWantToCreate);
    }

    @Override
    public short getMaxCapacity() {
        return maxCapacity;
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        this.adcList.forEach(adc -> {
            bridgeSpi.removeAdc(adc);
        });
    }

    @Override
    public String getCircuitBoardId() {
        return circuitBoardId;
    }

    @Override
    public String getVersionId() {
        return versionId;
    }

    @Override
    public Set<Adc> getAdcList() {
        return adcList;
    }

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        ;

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }
}