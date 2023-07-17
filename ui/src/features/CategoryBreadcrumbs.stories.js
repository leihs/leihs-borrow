import React from 'react'
import CategoryBreadcrumbs from './CategoryBreadcrumbs'
import PageLayout from '../components/PageLayout'
import PageLayoutMock from '../story-utils/PageLayoutMock'

export default {
  title: 'Feature Components/CategoryBreadcrumbs',
  component: CategoryBreadcrumbs,
  parameters: {
    layout: 'fullscreen'
  }
}

export const level2 = () => (
  <PageLayoutMock>
    <PageLayout.Header
      title="Bären"
      preTitle={
        <CategoryBreadcrumbs
          ancestorCats={[
            { id: '1', name: 'Wirbeltiere', url: 'cat/1' },
            { id: '2', name: 'Säugetiere', url: 'cat/1/2' }
          ]}
        />
      }
    ></PageLayout.Header>
    <p>
      Lorem ipsum dolor, sit amet consectetur adipisicing elit. Esse repudiandae non totam voluptatem voluptate
      architecto, eius exercitationem enim unde sunt nostrum neque iste consequuntur et perspiciatis quas praesentium?
      Recusandae, nesciunt?
    </p>
  </PageLayoutMock>
)

export const level1 = () => (
  <PageLayoutMock>
    <PageLayout.Header
      title="Säugetiere"
      preTitle={<CategoryBreadcrumbs ancestorCats={[{ id: '1', name: 'Wirbeltiere', url: 'cat/1' }]} />}
    ></PageLayout.Header>
    <p>
      Lorem ipsum dolor, sit amet consectetur adipisicing elit. Esse repudiandae non totam voluptatem voluptate
      architecto, eius exercitationem enim unde sunt nostrum neque iste consequuntur et perspiciatis quas praesentium?
      Recusandae, nesciunt?
    </p>
  </PageLayoutMock>
)

export const level0 = () => (
  <PageLayoutMock>
    <PageLayout.Header title="Wirbeltiere"></PageLayout.Header>
    <p>
      Lorem ipsum dolor, sit amet consectetur adipisicing elit. Esse repudiandae non totam voluptatem voluptate
      architecto, eius exercitationem enim unde sunt nostrum neque iste consequuntur et perspiciatis quas praesentium?
      Recusandae, nesciunt?
    </p>
  </PageLayoutMock>
)
