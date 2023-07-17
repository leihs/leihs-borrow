import React from 'react'

export default {
  title: 'Theme/Spacing'
}

export function spacing() {
  return (
    <div>
      <h1>Spacing</h1>
      <h2 className="text-muted">Schema</h2>
      <table className="table table-bordered table-sm text-muted">
        <tbody>
          <tr>
            <th>Spacing size</th>
            <td>1</td>
            <td>2</td>
            <td>3</td>
            <td>4</td>
            <td>5</td>
          </tr>
          <tr>
            <th>Bootstrap default</th>
            <td>4px</td>
            <td>8px</td>
            <td>16px</td>
            <td>24px</td>
            <td>48px</td>
          </tr>
          <tr>
            <th>App theme</th>
            <td>5px</td>
            <td>10px</td>
            <td>15px</td>
            <td>30px</td>
            <td>50px</td>
          </tr>
        </tbody>
      </table>
      <p className="text-muted">For instance:</p>
      <div className="bg-secondary">
        <div className="bg-body mb-1">.mb-1</div>
        <div className="bg-body mb-2">.mb-2</div>
        <div className="bg-body mb-3">.mb-3</div>
        <div className="bg-body mb-4">.mb-4</div>
        <div className="bg-body mb-5">.mb-5</div>
        <div className="bg-body">&nbsp;</div>
      </div>
      <h2 className="text-muted">HTML Elements</h2>
      <p className="text-muted">
        Headings, paragraphs and horizontal ruler have 15px bottom margin by default (corresponding to .mb-3). The
        horizontal ruler additionally has 15px top margin.
      </p>
      <div className="bg-secondary">
        <h1 className="bg-body">Heading 1</h1>
        <h2 className="bg-body">Heading 2</h2>
        <h3 className="bg-body">Heading 3</h3>
        <p className="bg-body">Paragraph</p>
        <div className="bg-body">Horizontal ruler below:</div>
        <hr />
        <div className="bg-body">&nbsp;</div>
      </div>
      <p className="text-muted">
        Note: There are design components to control spacing. Prefer them over directly applying spacing utilities like
        m-x or p-x.
      </p>
    </div>
  )
}
