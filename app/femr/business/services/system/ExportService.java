/*
     fEMR - fast Electronic Medical Records
     Copyright (C) 2014  Team fEMR

     fEMR is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     fEMR is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with fEMR.  If not, see <http://www.gnu.org/licenses/>. If
     you have any questions, contact <info@teamfemr.org>.
*/
package femr.business.services.system;

import femr.business.services.core.IExportService;
import femr.common.dtos.ServiceResponse;
import femr.common.models.ResearchExportItem;
import femr.data.daos.core.IResearchRepository;
import femr.util.calculations.dateUtils;
import femr.util.export.CsvFileBuilder;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ExportService implements IExportService {

    private final IResearchRepository researchEncounterRepository;

    @Inject
    public ExportService(IResearchRepository researchEncounterRepository) {
        this.researchEncounterRepository = researchEncounterRepository;
    }

    @Override
    public ServiceResponse<File> exportAllEncounters(Collection<Integer> tripIds){

        ServiceResponse<File> response = new ServiceResponse<>();

        // As new patients are encountered, generate a UUID to represent them in the export file
        Map<Integer, UUID> patientIdMap = new HashMap<>();

        List<ResearchExportItem> exportItems = this.researchEncounterRepository
                .findAllEncountersForTripIds(tripIds)
                .stream()
                .peek(encounter -> patientIdMap.putIfAbsent(encounter.getPatient().getId(), UUID.randomUUID()))
                .map(encounter -> {

                    ResearchExportItem item = new ResearchExportItem();
                    item.setPatientId(patientIdMap.get(encounter.getPatient().getId()));
                    item.setPatientCity(encounter.getPatient().getCity());
                    item.setGender(encounter.getPatient().getSex());
                    item.setBirthDate(dateUtils.convertTimeToDateString(new DateTime(encounter.getPatient().getAge())));
                    item.setDayOfVisit(dateUtils.convertTimeToDateString(encounter.getDateOfTriageVisit()));

                    // We should be able to assume a Mission Trip exists here since we are querying by tripIds
                    item.setTripId(encounter.getMissionTrip().getId());
                    item.setTripTeam(encounter.getMissionTrip().getMissionTeam().getName());
                    item.setTripCountry(encounter.getMissionTrip().getMissionCity().getMissionCountry().getName());

                    Integer age = (int) Math.floor(dateUtils.getAgeAsOfDateFloat(encounter.getPatient().getAge(), encounter.getDateOfTriageVisit()));
                    item.setAge(age);

                    encounter.getChiefComplaints()
                        .forEach(c -> item.getChiefComplaints().add(c.getValue()));

                    // TODO - need to take into account replacements, should we skip undispensed medications?
                    encounter.getPatientPrescriptions()
                        .stream()
                        .filter(p -> p.getDateDispensed() != null)
                        .forEach(p -> item.getDispensedMedications().add(p.getMedication().getName()));

                    encounter.getTabFields()
                        .forEach(tf -> {
                            // init to an empty list if not present
                            item.getTabFieldMap().putIfAbsent(tf.getTabField().getName(), new ArrayList<>());
                            item.getTabFieldMap().get(tf.getTabField().getName()).add(tf.getTabFieldValue());
                        });

                    encounter.getEncounterVitals()
                        .forEach(v -> {
                            item.getVitalMap().put(v.getVital().getName(), v.getVitalValue());
                        });

                    return item;

                })
                .collect(Collectors.toList());

        File exportedCsv = CsvFileBuilder.createCsvFile(exportItems);
        response.setResponseObject(exportedCsv);

        return response;
    }

}
