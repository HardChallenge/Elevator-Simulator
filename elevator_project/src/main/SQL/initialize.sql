begin
	DROP TABLE IF EXISTS Elevators;
	DROP TABLE IF EXISTS Trips;
	DROP TABLE IF EXISTS global_variables;
	DROP FUNCTION IF EXISTS no_data_exception;
	DROP FUNCTION IF EXISTS updated_already_exception;

	CREATE TABLE Elevators(
	id serial primary key,
	maxCapacity int,
	haveMirror boolean,
	backgroundColor varchar(30)
	);

	CREATE TABLE Trips(
	id serial primary key,
	elevatorId int,
	startedAt timestamp,
	stoppedAt timestamp,
	numberOfStops int,
	direction varchar(15),
	weightTransported int
	);

	create table global_variables(
		id serial primary key,
		name varchar(100),
		value varchar(100));

	INSERT INTO global_variables(name, value) values ('rowsRead', 0::text);
	INSERT INTO global_variables(name, value) values ('statisticsPath', '/Users/razvanchichirau/Desktop/Elevators/Elevator-Simulator/elevator_project/statistics.txt');

	create or replace function no_data_exception() returns TEXT as $$
	begin
	RETURN 'No data in trips available';
	end;
	$$
	language plpgsql;

	create or replace function updated_already_exception() returns TEXT as $$
	begin
	RETURN 'Already updated, no new data in trips.';
	end;
	$$
	language plpgsql;
end;