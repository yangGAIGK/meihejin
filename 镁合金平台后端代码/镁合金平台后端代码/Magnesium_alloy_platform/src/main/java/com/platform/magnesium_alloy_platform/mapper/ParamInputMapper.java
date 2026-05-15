package com.platform.magnesium_alloy_platform.mapper;

import com.platform.magnesium_alloy_platform.pojo.BPParameter;
import com.platform.magnesium_alloy_platform.pojo.PSOParameter;
import com.platform.magnesium_alloy_platform.pojo.GAParameter;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ParamInputMapper {

	@Insert("INSERT INTO bp_parameter (input_layer, output_layer, intermediate_layer, options, number_of_cycles, learning_rate, error_target_value, create_user, create_time, update_time) " +
			"VALUES (#{inputLayer}, #{outputLayer}, #{intermediateLayer}, #{options}, #{numberOfCycles}, #{learningRate}, #{errorTargetValue}, #{createUser}, NOW(), NOW())")
	void insertBPParameter(BPParameter bpParameter);

	@Insert("INSERT INTO pso_parameter (max_num, top_value, low_value, swarm_size, individual_factor, group_factor, inertia_factor, create_user, create_time, update_time) " +
			"VALUES (#{maxNum}, #{topValue}, #{lowValue}, #{swarmSize}, #{individualFactor}, #{groupFactor}, #{inertiaFactor}, #{createUser}, NOW(), NOW())")
	void insertPSOParameter(PSOParameter psoParameter);

	@Insert("INSERT INTO ga_parameter (crossover_probability, variation_probability, create_user, create_time, update_time) " +
			"VALUES (#{crossoverProbability}, #{variationProbability}, #{createUser}, NOW(), NOW())")
	void insertGAParameter(GAParameter gaParameter);
}
