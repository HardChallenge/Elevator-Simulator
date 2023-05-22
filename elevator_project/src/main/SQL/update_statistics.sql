create or replace function update_statistics() returns void as $$
declare
filePath TEXT;
updatedContent TEXT = '';
rowsRead integer;
rowsTrips integer;
fetchElevatorIds CURSOR FOR SELECT id from elevators;
elevId integer;
averageWeight REAL;
maximumWeight integer;
averageDuration INTERVAL;
averageNumberOfStops REAL;
dataUpdatedException TEXT;
noDataException TEXT;
begin
	-- Check if the data can be updated
	SELECT value INTO rowsRead from global_variables where name = 'rowsRead';
	SELECT COUNT(*) INTO rowsTrips from trips;

	IF rowsTrips != 0 AND rowsRead = rowsTrips THEN
		SELECT updated_already_exception() INTO dataUpdatedException;
		RAISE EXCEPTION '%', dataUpdatedException;
	END IF;

	IF rowsTrips = 0 THEN
		SELECT no_data_exception() INTO noDataException;
		RAISE EXCEPTION '%', noDataException;
	END IF;

	UPDATE global_variables SET value = rowsTrips where name = 'rowsRead';
	-----------------------------------

	SELECT value INTO filePath from global_variables where name = 'statisticsPath';

	-- Process the data
	OPEN fetchElevatorIds;

	LOOP
        FETCH NEXT FROM fetchElevatorIds INTO elevId;
        EXIT WHEN NOT FOUND;

		elevId := elevId - 1;

        updatedContent := updatedContent ||'Elevator with ID ' || elevId || ' has the next statistics: ' || CHR(10);

		SELECT avg(weighttransported), max(weighttransported), avg(stoppedAt - startedAt), avg(numberofstops)
        INTO averageWeight, maximumWeight, averageDuration, averageNumberOfStops
        FROM trips WHERE trips.elevatorid = elevId;

		updatedContent := updatedContent || '    Average weight transported -> ' || CASE WHEN averageWeight IS NULL THEN 'N/A' ELSE averageWeight::TEXT END || CHR(10);
		updatedContent := updatedContent || '    Maximum weight transported in one go -> ' || CASE WHEN maximumWeight IS NULL THEN 'N/A' ELSE maximumWeight::TEXT END || CHR(10);
		updatedContent := updatedContent || '    Average duration of a trip -> ' ||
		CASE WHEN averageDuration is NULL THEN 'N/A'
		ELSE to_char(averageDuration, 'HH24:MI:SS')
		END
		|| CHR(10);
		updatedContent := updatedContent || '    Average number of stops in one direction -> ' || CASE WHEN averageNumberOfStops IS NULL THEN 'N/A' ELSE averageNumberOfStops::TEXT END || CHR(10);

		updatedContent := updatedContent || '=======================================' || CHR(10);
    END LOOP;

	CLOSE fetchElevatorIds;

	EXECUTE format($fmt$ copy (select regexp_split_to_table(e'%s', e'\n')) to '%s'
        $fmt$, updatedContent, filePath);
end;
$$
language plpgsql;