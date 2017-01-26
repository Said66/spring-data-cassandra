/*
 * Copyright 2013-2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.cassandra.mapping;

import static org.springframework.cassandra.core.cql.CqlIdentifier.*;
import static org.springframework.cassandra.core.keyspace.CreateTableSpecification.*;
import static org.springframework.data.cassandra.mapping.CassandraSimpleTypeHolder.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.cassandra.core.cql.CqlIdentifier;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.CreateUserTypeSpecification;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.cassandra.convert.CustomConversions;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.UserType;

/**
 * Default implementation of a {@link MappingContext} for Cassandra using {@link CassandraPersistentEntity} and
 * {@link CassandraPersistentProperty} as primary abstractions.
 *
 * @author Alex Shvid
 * @author Matthew T. Adams
 * @author Mark Paluch
 * @author John Blum
 */
public class BasicCassandraMappingContext
		extends AbstractMappingContext<CassandraPersistentEntity<?>, CassandraPersistentProperty>
		implements CassandraMappingContext, ApplicationContextAware {

	protected ApplicationContext context;

	protected CassandraPersistentEntityMetadataVerifier verifier = new CompositeCassandraPersistentEntityMetadataVerifier();

	protected ClassLoader beanClassLoader;

	protected Mapping mapping = new Mapping();

	// useful caches
	protected Map<Class<?>, CassandraPersistentEntity<?>> entitiesByType = new HashMap<>();
	protected Map<CqlIdentifier, Set<CassandraPersistentEntity<?>>> entitySetsByTableName = new HashMap<>();

	protected Set<CassandraPersistentEntity<?>> primaryKeyEntities = new HashSet<>();
	protected Set<CassandraPersistentEntity<?>> userDefinedTypes = new HashSet<>();
	protected Set<CassandraPersistentEntity<?>> tableEntities = new HashSet<>();

	private CustomConversions customConversions;

	private UserTypeResolver userTypeResolver;

	/**
	 * Creates a new {@link BasicCassandraMappingContext}.
	 */
	public BasicCassandraMappingContext() {
		setCustomConversions(new CustomConversions(Collections.EMPTY_LIST));
		setSimpleTypeHolder(CassandraSimpleTypeHolder.HOLDER);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void initialize() {
		super.initialize();
		processMappingOverrides();
	}

	/* (non-Javadoc) */
	@SuppressWarnings("all")
	protected void processMappingOverrides() {

		mapping.getEntityMappings().stream().filter((entityMapping -> entityMapping != null)).forEach(entityMapping -> {
			String entityClassName = entityMapping.getEntityClassName();

			try {
				Class<?> entityClass = ClassUtils.forName(entityClassName, beanClassLoader);

				CassandraPersistentEntity<?> entity = getRequiredPersistentEntity(entityClass);

				String entityTableName = entityMapping.getTableName();

				if (StringUtils.hasText(entityTableName)) {
					entity.setTableName(cqlId(entityTableName, Boolean.valueOf(entityMapping.getForceQuote())));
				}

				processMappingOverrides(entity, entityMapping);

			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(String.format("Unknown persistent entity name [%s]", entityClassName), e);
			}
		});
	}

	/* (non-Javadoc) */
	protected void processMappingOverrides(CassandraPersistentEntity<?> entity, EntityMapping entityMapping) {
		entityMapping.getPropertyMappings()
				.forEach((key, propertyMapping) -> processMappingOverride(entity, propertyMapping));
	}

	/* (non-Javadoc) */
	protected void processMappingOverride(CassandraPersistentEntity<?> entity, PropertyMapping mapping) {

		CassandraPersistentProperty property = entity.getRequiredPersistentProperty(mapping.getPropertyName());

		boolean forceQuote = Boolean.valueOf(mapping.getForceQuote());

		property.setForceQuote(forceQuote);

		if (StringUtils.hasText(mapping.getColumnName())) {
			property.setColumnName(cqlId(mapping.getColumnName(), forceQuote));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	/**
	 * Sets the {@link CustomConversions}.
	 *
	 * @param customConversions must not be {@literal null}.
	 * @since 1.5
	 */
	public void setCustomConversions(CustomConversions customConversions) {

		Assert.notNull(customConversions, "CustomConversions must not be null");

		this.customConversions = customConversions;
	}

	public void setMapping(Mapping mapping) {

		Assert.notNull(mapping, "Mapping must not be null");

		this.mapping = mapping;
	}

	/**
	 * Sets the {@link UserTypeResolver}.
	 *
	 * @param userTypeResolver must not be {@literal null}.
	 * @since 1.5
	 */
	public void setUserTypeResolver(UserTypeResolver userTypeResolver) {

		Assert.notNull(userTypeResolver, "UserTypeResolver must not be null");

		this.userTypeResolver = userTypeResolver;
	}

	/**
	 * @param verifier The verifier to set.
	 */
	@Override
	public void setVerifier(CassandraPersistentEntityMetadataVerifier verifier) {
		this.verifier = verifier;
	}

	/**
	 * @return Returns the verifier.
	 */
	@SuppressWarnings("unused")
	public CassandraPersistentEntityMetadataVerifier getVerifier() {
		return verifier;
	}

	@Override
	public Collection<CassandraPersistentEntity<?>> getNonPrimaryKeyEntities() {
		return getTableEntities();
	}

	@Override
	public Collection<CassandraPersistentEntity<?>> getPrimaryKeyEntities() {
		return Collections.unmodifiableSet(primaryKeyEntities);
	}

	@Override
	public Collection<CassandraPersistentEntity<?>> getTableEntities() {
		return Collections.unmodifiableCollection(tableEntities);
	}

	@Override
	public Collection<CassandraPersistentEntity<?>> getUserDefinedTypeEntities() {
		return Collections.unmodifiableSet(userDefinedTypes);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.cassandra.mapping.CassandraMappingContext#getPersistentEntities(boolean)
	 */
	@Override
	public Collection<CassandraPersistentEntity<?>> getPersistentEntities(boolean includePrimaryKeyTypesAndUdts) {

		if (includePrimaryKeyTypesAndUdts) {
			return super.getPersistentEntities();
		}

		return getTableEntities();
	}

	@Override
	protected <T> CassandraPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {

		UserDefinedType userDefinedType = AnnotatedElementUtils.findMergedAnnotation(typeInformation.getType(),
				UserDefinedType.class);

		CassandraPersistentEntity<T> entity;

		if (userDefinedType != null) {
			entity = new CassandraUserTypePersistentEntity<>(typeInformation, this, verifier, userTypeResolver);
			userDefinedTypes.add(entity);
		} else {
			entity = new BasicCassandraPersistentEntity<>(typeInformation, this, verifier);
		}

		if (context != null) {
			entity.setApplicationContext(context);
		}

		// now do some caching of the entity

		Set<CassandraPersistentEntity<?>> entities = entitySetsByTableName.computeIfAbsent(entity.getTableName(),
				cqlIdentifier -> new HashSet<>());

		entities.add(entity);

		if (!entity.isUserDefinedType()) {
			if (entity.isCompositePrimaryKey()) {
				primaryKeyEntities.add(entity);
			}

			entity.findAnnotation(Table.class).ifPresent(table -> tableEntities.add(entity));
		}

		entitiesByType.put(entity.getType(), entity);

		return entity;
	}

	@Override
	protected CassandraPersistentProperty createPersistentProperty(Property property, CassandraPersistentEntity<?> owner,
			SimpleTypeHolder simpleTypeHolder) {
		return createPersistentProperty(property, owner, (CassandraSimpleTypeHolder) simpleTypeHolder);
	}

	public CassandraPersistentProperty createPersistentProperty(Property property, CassandraPersistentEntity<?> owner,
			CassandraSimpleTypeHolder simpleTypeHolder) {

		return new BasicCassandraPersistentProperty(property, owner, simpleTypeHolder, userTypeResolver);
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.cassandra.mapping.CassandraMappingContext#usesTable(com.datastax.driver.core.TableMetadata)
	 */
	@Override
	public boolean usesTable(TableMetadata table) {
		return entitySetsByTableName.containsKey(cqlId(table.getName()));
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.cassandra.mapping.CassandraMappingContext#usesUserType(com.datastax.driver.core.UserType)
	 */
	@Override
	public boolean usesUserType(final UserType userType) {

		CqlIdentifier identifier = CqlIdentifier.cqlId(userType.getTypeName());

		return (hasMappedUserType(identifier) || hasReferencedUserType(identifier));
	}

	private boolean hasReferencedUserType(final CqlIdentifier identifier) {

		return getPersistentEntities().stream().anyMatch(entity -> {

			return entity.getPersistentProperties().anyMatch(property -> {

				Optional<CassandraType> optionalCassandraType = property.findAnnotation(CassandraType.class);

				return optionalCassandraType.map(cassandraType -> {
					return StringUtils.hasText(cassandraType.userTypeName())
							&& CqlIdentifier.cqlId(cassandraType.userTypeName()).equals(identifier);
				}).orElse(false);
			});
		});

	}

	private boolean hasMappedUserType(CqlIdentifier identifier) {
		return userDefinedTypes.stream().map(CassandraPersistentEntity::getTableName).anyMatch(identifier::equals);
	}

	@Override
	public CreateTableSpecification getCreateTableSpecificationFor(CassandraPersistentEntity<?> entity) {

		Assert.notNull(entity, "CassandraPersistentEntity must not be null");

		final CreateTableSpecification specification = createTable().name(entity.getTableName());

		entity.getPersistentProperties().filter(CassandraPersistentProperty::isCompositePrimaryKey).forEach(property -> {

			CassandraPersistentEntity<?> primaryKeyEntity = getRequiredPersistentEntity(property.getRawType());

			primaryKeyEntity.getPersistentProperties().forEach(primaryKeyProperty -> {

				if (primaryKeyProperty.isPartitionKeyColumn()) {
					specification.partitionKeyColumn(primaryKeyProperty.getColumnName(), getDataType(primaryKeyProperty));
				} else { // it's a cluster column
					specification.clusteredKeyColumn(primaryKeyProperty.getColumnName(), getDataType(primaryKeyProperty),
							primaryKeyProperty.getPrimaryKeyOrdering());
				}

			});

		});

		entity.getPersistentProperties().filter((property) -> !property.isCompositePrimaryKey()).forEach(property -> {
			if (property.isIdProperty() || property.isPartitionKeyColumn()) {
				specification.partitionKeyColumn(property.getColumnName(), getDataType(property));
			} else if (property.isClusterKeyColumn()) {
				specification.clusteredKeyColumn(property.getColumnName(), getDataType(property),
						property.getPrimaryKeyOrdering());
			} else {
				specification.column(property.getColumnName(), getDataType(property));
			}
		});

		if (specification.getPartitionKeyColumns().isEmpty()) {
			throw new MappingException(String.format("No partition key columns found in entity [%s]", entity.getType()));
		}

		return specification;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.cassandra.mapping.CassandraMappingContext#getCreateUserTypeSpecificationFor(org.springframework.data.cassandra.mapping.CassandraPersistentEntity)
	 */
	@Override
	public CreateUserTypeSpecification getCreateUserTypeSpecificationFor(CassandraPersistentEntity<?> entity) {

		Assert.notNull(entity, "CassandraPersistentEntity must not be null");

		final CreateUserTypeSpecification specification = CreateUserTypeSpecification.createType(entity.getTableName());

		entity.doWithProperties(new PropertyHandler<CassandraPersistentProperty>() {

			@Override
			public void doWithPersistentProperty(CassandraPersistentProperty property) {
				specification.field(property.getColumnName(), getDataType(property));
			}
		});

		if (specification.getFields().isEmpty()) {
			throw new MappingException(String.format("No fields in user type [%s]", entity.getType()));
		}

		return specification;
	}

	/* (non-Javadoc)
		 * @see org.springframework.data.mapping.context.AbstractMappingContext#shouldCreatePersistentEntityFor(org.springframework.data.util.TypeInformation)
		 */
	@Override
	protected boolean shouldCreatePersistentEntityFor(TypeInformation<?> typeInfo) {
		return (!customConversions.hasCustomWriteTarget(typeInfo.getType())
				&& super.shouldCreatePersistentEntityFor(typeInfo));
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.mapping.context.AbstractMappingContext#addPersistentEntity(org.springframework.data.util.TypeInformation)
	 */
	@Override
	protected Optional<CassandraPersistentEntity<?>> addPersistentEntity(TypeInformation<?> typeInformation) {
		// Prevent conversion types created as CassandraPersistentEntity
		return (shouldCreatePersistentEntityFor(typeInformation) ? super.addPersistentEntity(typeInformation)
				: Optional.empty());
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.cassandra.mapping.CassandraMappingContext#getDataType(org.springframework.data.cassandra.mapping.CassandraPersistentProperty)
	 */
	@Override
	public DataType getDataType(CassandraPersistentProperty property) {

		if (property.isCompositePrimaryKey()) {
			return property.getDataType();
		}

		if (property.findAnnotation(CassandraType.class).isPresent()) {
			return property.getDataType();
		}

		Optional<CassandraPersistentEntity<?>> persistentEntity = getPersistentEntity(property.getType());

		if (persistentEntity.filter(CassandraPersistentEntity::isUserDefinedType).isPresent()) {
			return persistentEntity.get().getUserType();
		}

		if (customConversions.hasCustomWriteTarget(property.getType())) {
			return getDataTypeFor(customConversions.getCustomWriteTarget(property.getType()));
		}

		if (customConversions.hasCustomWriteTarget(property.getActualType())) {
			Class<?> targetType = customConversions.getCustomWriteTarget(property.getActualType());

			if (property.isCollectionLike()) {
				if (List.class.isAssignableFrom(property.getType())) {
					return DataType.list(getDataTypeFor(targetType));
				}

				if (Set.class.isAssignableFrom(property.getType())) {
					return DataType.set(getDataTypeFor(targetType));
				}
			}

			return getDataTypeFor(targetType);
		}

		return property.getDataType();
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.cassandra.mapping.CassandraMappingContext#getDataType(java.lang.Class)
	 */
	@Override
	public DataType getDataType(Class<?> type) {
		return (customConversions.hasCustomWriteTarget(type) ? getDataTypeFor(customConversions.getCustomWriteTarget(type))
				: getDataTypeFor(type));
	}

	@Override
	public CassandraPersistentEntity<?> getExistingPersistentEntity(Class<?> type) {

		CassandraPersistentEntity<?> entity = entitiesByType.get(type);

		Assert.notNull(entity, String.format("Unknown persistent type [%s]", type.getName()));

		return entity;
	}

	@Override
	public boolean contains(Class<?> type) {
		return entitiesByType.containsKey(type);
	}
}
